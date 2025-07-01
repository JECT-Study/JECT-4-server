package com.ject.studytrip.auth.infra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record KakaoUserInfoResponse(
        @Schema(description = "카카오 ID") @JsonProperty("id") String kakaoId,
        @Schema(description = "카카오 계정") @JsonProperty("kakao_account") KakaoAccount account) {
    public String getProfileImage() {
        return account.profile().profileImage();
    }

    public String getEmail() {
        return account.email();
    }
}
