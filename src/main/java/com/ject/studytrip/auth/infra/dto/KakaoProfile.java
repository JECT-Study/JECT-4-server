package com.ject.studytrip.auth.infra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record KakaoProfile(
        @Schema(description = "카카오 프로필 이미지") @JsonProperty("profile_image_url")
                String profileImage) {}
