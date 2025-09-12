package com.ject.studytrip.auth.application.dto;

public record TokenInfo(String accessToken, String refreshToken, long refreshTokenExpiresIn) {
    public static TokenInfo of(
            String accessToken, String refreshToken, long refreshTokenExpiresIn) {
        return new TokenInfo(accessToken, refreshToken, refreshTokenExpiresIn);
    }
}
