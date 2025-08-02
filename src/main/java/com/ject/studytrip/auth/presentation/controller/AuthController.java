package com.ject.studytrip.auth.presentation.controller;

import com.ject.studytrip.auth.application.facade.AuthFacade;
import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest;
import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest;
import com.ject.studytrip.auth.presentation.dto.request.LogoutRequest;
import com.ject.studytrip.auth.presentation.dto.request.TokenReissueRequest;
import com.ject.studytrip.auth.presentation.dto.response.TokenResponse;
import com.ject.studytrip.global.common.response.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController {
    private final AuthFacade authFacade;

    @Operation(summary = "카카오 로그인", description = "카카오 인가 코드를 이용하여, 엑세스 토큰과 리프레시 토큰을 발급합니다.")
    @PostMapping("/login/kakao")
    public ResponseEntity<StandardResponse> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request) {
        TokenResponse response = authFacade.kakaoLogin(request);
        return ResponseEntity.ok(StandardResponse.success(HttpStatus.OK.value(), response));
    }

    @Operation(
            summary = "카카오 회원가입",
            description = "카카오 인가 코드, 카테고리, 닉네임을 이용하여, 엑세스 토큰과 리프레시 토큰을 발급합니다.")
    @PostMapping("/signup/kakao")
    public ResponseEntity<StandardResponse> kakaoSignup(
            @Valid @RequestBody KakaoSignupRequest request) {
        TokenResponse response = authFacade.kakaoSignup(request);
        return ResponseEntity.ok(StandardResponse.success(HttpStatus.OK.value(), response));
    }

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 이용하여, 엑세스 토큰과 리프레시 토큰을 재발급합니다.")
    @PostMapping("/token/reissue")
    public ResponseEntity<StandardResponse> reissueToken(
            @Valid @RequestBody TokenReissueRequest request) {
        TokenResponse response = authFacade.reissueToken(request);
        return ResponseEntity.ok(StandardResponse.success(HttpStatus.OK.value(), response));
    }

    @Operation(
            summary = "로그아웃",
            description = "엑세스 토큰과 리프레시 토큰을 이용하여, 엑세스 토큰을 블랙리스트에 추가하고, 저장된 리프레시 토큰을 제거합니다.")
    @PostMapping("/logout")
    public ResponseEntity<StandardResponse> logout(@Valid @RequestBody LogoutRequest request) {
        authFacade.logout(request);
        return ResponseEntity.ok(StandardResponse.success(HttpStatus.OK.value(), null));
    }
}
