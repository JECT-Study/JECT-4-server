package com.ject.studytrip.auth.presentation.controller;

import static com.ject.studytrip.global.common.constants.CookieConstants.*;

import com.ject.studytrip.auth.application.dto.OAuthLoginOutcome;
import com.ject.studytrip.auth.application.dto.TokenInfo;
import com.ject.studytrip.auth.application.facade.AuthFacade;
import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest;
import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest;
import com.ject.studytrip.auth.presentation.dto.request.LogoutRequest;
import com.ject.studytrip.auth.presentation.dto.response.LoginResponse;
import com.ject.studytrip.auth.presentation.dto.response.ReissueTokenResponse;
import com.ject.studytrip.auth.presentation.helper.AuthCookieHelper;
import com.ject.studytrip.global.common.response.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthFacade authFacade;

    @Operation(
            summary = "카카오 로그인",
            description =
                    """
                    카카오 인가 코드를 이용하여 로그인을 수행합니다.
                    가입된 회원이라면 엑세스 토큰(Response Body)과 리프레시 토큰(HttpOnly Secure 쿠키 - 'auth_refresh')을 반환합니다.
                    가입되지 않은 회원이라면 OAuth 가입키('oauth_signup_key')를 HttpOnly Secure 쿠키에 담아 응답합니다.
                """)
    @PostMapping("/login/kakao")
    public ResponseEntity<StandardResponse> kakaoLogin(
            @RequestAttribute(value = "origin") String origin,
            @Valid @RequestBody KakaoLoginRequest request) {
        OAuthLoginOutcome response = authFacade.kakaoLogin(request, origin);

        // 회원가입이 필요할 경우
        // 카카오 유저 프로필 키 저장 (쿠키)
        if (response.isSignupRequired()) {
            ResponseCookie pendingCookie =
                    AuthCookieHelper.setOAuthSignupProfileCookie(response.signupKey());
            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, pendingCookie.toString())
                    .body(
                            StandardResponse.success(
                                    HttpStatus.OK.value(), LoginResponse.requiredSignup()));
        }

        // 리프레시 토큰 저장 (쿠키)
        ResponseCookie refreshCookie =
                AuthCookieHelper.setRefreshTokenCookie(
                        response.tokenInfo().refreshToken(),
                        response.tokenInfo().refreshTokenExpiresIn());
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(
                        StandardResponse.success(
                                HttpStatus.OK.value(),
                                LoginResponse.success(response.tokenInfo().accessToken())));
    }

    @Operation(
            summary = "카카오 회원가입",
            description =
                    """
                        닉네임, 카테고리를 입력받고, OAuth 가입키 쿠키로 회원가입을 수행합니다.
                        회원가입에 성공하면 로그인처리되며, 엑세스 토큰(Response Body)과 리프레시 토큰(HttpOnly Secure 쿠키 - 'auth_refresh')을 반환합니다.
                    """)
    @PostMapping("/signup/kakao")
    public ResponseEntity<StandardResponse> kakaoSignup(
            @CookieValue(name = OAUTH_SIGNUP_KEY, required = false) String pendingKey,
            @Valid @RequestBody KakaoSignupRequest request) {
        TokenInfo response = authFacade.kakaoSignup(pendingKey, request);

        // 리프레시 토큰 쿠키 생성
        ResponseCookie refreshCookie =
                AuthCookieHelper.setRefreshTokenCookie(
                        response.refreshToken(), response.refreshTokenExpiresIn());

        // 카카오 가입 쿠키 삭제
        ResponseCookie clearSignupCookie = AuthCookieHelper.clearCookie(OAUTH_SIGNUP_KEY);

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, clearSignupCookie.toString())
                .body(
                        StandardResponse.success(
                                HttpStatus.OK.value(),
                                LoginResponse.success(response.accessToken())));
    }

    @Operation(
            summary = "토큰 재발급",
            description =
                    "리프레시 토큰을 이용하여, 엑세스 토큰과 리프레시 토큰을 재발급합니다. 리프레시 토큰은 'auth_refresh' HttpOnly Secure 쿠키에 담아 응답합니다.")
    @PostMapping("/token/reissue")
    public ResponseEntity<StandardResponse> reissueToken(
            @CookieValue(name = AUTH_REFRESH_TOKEN, required = false) String refreshToken) {
        TokenInfo response = authFacade.reissueToken(refreshToken);

        // 리프레시 토큰 덮어쓰기
        ResponseCookie refreshCookie =
                AuthCookieHelper.setRefreshTokenCookie(
                        response.refreshToken(), response.refreshTokenExpiresIn());
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(
                        StandardResponse.success(
                                HttpStatus.OK.value(),
                                ReissueTokenResponse.of(response.accessToken())));
    }

    @Operation(
            summary = "로그아웃",
            description = "엑세스 토큰과 리프레시 토큰을 이용하여, 엑세스 토큰을 블랙리스트에 추가하고, 저장된 리프레시 토큰을 제거합니다.")
    @PostMapping("/logout")
    public ResponseEntity<StandardResponse> logout(
            @CookieValue(name = AUTH_REFRESH_TOKEN, required = false) String refreshToken,
            @Valid @RequestBody LogoutRequest request) {
        authFacade.logout(request, refreshToken);

        // 기존 리프레시 쿠키 삭제
        ResponseCookie clearCookie = AuthCookieHelper.clearCookie(AUTH_REFRESH_TOKEN);
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }
}
