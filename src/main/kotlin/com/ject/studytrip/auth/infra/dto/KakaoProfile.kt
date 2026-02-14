package com.ject.studytrip.auth.infra.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class KakaoProfile(
    @field:Schema(description = "카카오 프로필 이미지")
    @field:JsonProperty("profile_image_url")
    val profileImage: String,
)
