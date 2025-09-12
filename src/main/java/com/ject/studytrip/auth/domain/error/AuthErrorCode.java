package com.ject.studytrip.auth.domain.error;

import com.ject.studytrip.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 부족합니다."),

    // 카카오 로그인 관련 에러
    KAKAO_TOKEN_FETCH_FAILED(HttpStatus.UNAUTHORIZED, "카카오 토큰을 가져오는 데 실패했습니다."),
    KAKAO_USER_INFO_FETCH_FAILED(HttpStatus.UNAUTHORIZED, "카카오 사용자 정보를 가져오는 데 실패했습니다."),
    INVALID_KAKAO_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 카카오 액세스 토큰입니다."),
    INVALID_KAKAO_AUTHORIZATION_CODE(HttpStatus.BAD_REQUEST, "잘못된 카카오 인가 코드입니다."),
    KAKAO_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "카카오 서버에서 오류가 발생했습니다."),
    MISSING_KAKAO_SIGNUP_KEY(HttpStatus.BAD_REQUEST, "카카오 가입 키(signupKey)가 누락되었습니다."),
    INVALID_KAKAO_SIGNUP_KEY(
            HttpStatus.BAD_REQUEST, "요청한 카카오 가입 키(signupKey)의 정보가 존재하지 않거나 만료되었습니다."),

    // 인증 관련 예외
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 JWT 토큰입니다."),
    MISSING_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰이 누락되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 리프레시 토큰입니다."),
    TOKEN_IS_BLACKLISTED(HttpStatus.UNAUTHORIZED, "블랙리스트된 엑세스 토큰입니다."),
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
