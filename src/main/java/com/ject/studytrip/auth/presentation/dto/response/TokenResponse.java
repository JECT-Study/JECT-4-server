package com.ject.studytrip.auth.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "엑세스 토큰") String accessToken,
        @Schema(description = "리프레시 토큰") String refreshToken) {
    public static TokenResponse of(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken);
    }
}
