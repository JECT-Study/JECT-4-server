package com.ject.studytrip.auth.presentation.controller

import com.ject.studytrip.auth.application.dto.OAuthLoginOutcome
import com.ject.studytrip.auth.application.facade.AuthFacade
import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest
import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest
import com.ject.studytrip.auth.presentation.dto.request.LogoutRequest
import com.ject.studytrip.auth.presentation.dto.response.LoginResponse
import com.ject.studytrip.auth.presentation.dto.response.ReissueTokenResponse
import com.ject.studytrip.auth.presentation.helper.AuthCookieHelper
import com.ject.studytrip.global.common.constants.CookieConstants.AUTH_REFRESH_TOKEN
import com.ject.studytrip.global.common.constants.CookieConstants.OAUTH_SIGNUP_KEY
import com.ject.studytrip.global.common.response.StandardResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@Validated
class AuthController(
    private val authFacade: AuthFacade,
) {
    @Operation(
        summary = "카카오 회원가입",
        description = """
            닉네임, 카테고리를 입력받고, OAuth 가입키 쿠키로 회원가입을 수행합니다.
            회원가입에 성공하면 로그인처리되며, 엑세스 토큰(Response Body)과 리프레시 토큰(HttpOnly Secure 쿠키 - 'auth_refresh')을 반환합니다.
        """,
    )
    @PostMapping("/signup/kakao")
    fun kakaoSignup(
        @CookieValue(name = OAUTH_SIGNUP_KEY, required = false) pendingKey: String?,
        @RequestBody @Valid request: KakaoSignupRequest,
    ): ResponseEntity<StandardResponse> {
        val response = authFacade.kakaoSignup(pendingKey, request)

        // 리프레시 토큰 쿠키 생성
        val refreshCookie = AuthCookieHelper.setRefreshTokenCookie(response.refreshToken, response.refreshTokenExpiresIn)

        // 카카오 가입 쿠키 삭제
        val clearSignupCookie = AuthCookieHelper.clearCookie(OAUTH_SIGNUP_KEY)

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .header(HttpHeaders.SET_COOKIE, clearSignupCookie.toString())
            .body(StandardResponse.success(HttpStatus.OK.value(), LoginResponse.success(response.accessToken)))
    }

    @Operation(
        summary = "카카오 로그인",
        description = """
            카카오 인가 코드를 이용하여 로그인을 수행합니다.
            가입된 회원이라면 엑세스 토큰(Response Body)과 리프레시 토큰(HttpOnly Secure 쿠키 - 'auth_refresh')을 반환합니다.
            가입되지 않은 회원이라면 OAuth 가입키('oauth_signup_key')를 HttpOnly Secure 쿠키에 담아 응답합니다.
    """,
    )
    @PostMapping("/login/kakao")
    fun kakaoLogin(
        @RequestAttribute(value = "origin") origin: String,
        @RequestBody @Valid request: KakaoLoginRequest,
    ): ResponseEntity<StandardResponse> =
        when (val response = authFacade.kakaoLogin(request, origin)) {
            is OAuthLoginOutcome.SignupRequired -> {
                val cookie = AuthCookieHelper.setOAuthSignupProfileCookie(response.signupKey)

                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(StandardResponse.success(HttpStatus.OK.value(), LoginResponse.requiredSignup()))
            }

            is OAuthLoginOutcome.Success -> {
                val tokenInfo = response.tokenInfo
                val cookie = AuthCookieHelper.setRefreshTokenCookie(tokenInfo.refreshToken, tokenInfo.refreshTokenExpiresIn)

                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(StandardResponse.success(HttpStatus.OK.value(), LoginResponse.success(tokenInfo.accessToken)))
            }
        }

    @Operation(
        summary = "토큰 재발급",
        description = """
            리프레시 토큰을 이용하여, 엑세스 토큰과 리프레시 토큰을 재발급합니다.
            리프레시 토큰은 'auth_refresh' HttpOnly Secure 쿠키에 담아 응답합니다.
        """,
    )
    @PostMapping("/token/reissue")
    fun reissueToken(
        @CookieValue(name = AUTH_REFRESH_TOKEN, required = false) refreshToken: String?,
    ): ResponseEntity<StandardResponse> {
        val response = authFacade.reissueToken(refreshToken)

        // 리프레시 토큰 덮어쓰기
        val refreshCookie = AuthCookieHelper.setRefreshTokenCookie(response.refreshToken, response.refreshTokenExpiresIn)

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(StandardResponse.success(HttpStatus.OK.value(), ReissueTokenResponse(response.accessToken)))
    }

    @Operation(summary = "로그아웃", description = "엑세스 토큰과 리프레시 토큰을 이용하여, 엑세스 토큰을 블랙리스트에 추가하고, Redis에 저장된 리프레시 토큰을 제거합니다.")
    @PostMapping("/logout")
    fun logout(
        @CookieValue(name = AUTH_REFRESH_TOKEN, required = false) refreshToken: String?,
        @RequestBody @Valid request: LogoutRequest,
    ): ResponseEntity<StandardResponse> {
        authFacade.logout(request, refreshToken)

        // 기존 리프레시 쿠키 삭제
        val clearCookie = AuthCookieHelper.clearCookie(AUTH_REFRESH_TOKEN)

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
            .body(StandardResponse.success(HttpStatus.OK.value(), null))
    }
}
