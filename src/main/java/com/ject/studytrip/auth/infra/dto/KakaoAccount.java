package com.ject.studytrip.auth.infra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record KakaoAccount(
        @Schema(description = "카카오 프로필") @JsonProperty("profile") KakaoProfile profile,
        @Schema(description = "카카오 이메일") @JsonProperty("email") String email) {}
