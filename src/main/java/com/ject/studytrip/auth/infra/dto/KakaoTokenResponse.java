package com.ject.studytrip.auth.infra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record KakaoTokenResponse(
        @Schema(description = "카카오 토큰 타입") @JsonProperty("token_type") String tokenType,
        @Schema(description = "카카오 엑세스 토큰") @JsonProperty("access_token") String accessToken,
        @Schema(description = "카카오 엑세스 토큰 만료 시간") @JsonProperty("expires_in")
                Integer AccessExpiresIn,
        @Schema(description = "카카오 리프레시 토큰") @JsonProperty("refresh_token") String refreshToken,
        @Schema(description = "카카오 리프레시 토큰 만료 시간") @JsonProperty("refresh_token_expires_in")
                Integer RefreshExpiresIn,
        @Schema(description = "카카오 스코프") @JsonProperty("scope") String scope) {}
