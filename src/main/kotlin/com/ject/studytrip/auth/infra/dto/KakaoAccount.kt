package com.ject.studytrip.auth.infra.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class KakaoAccount(
    @field:Schema(description = "카카오 프로필")
    @field:JsonProperty("profile")
    val profile: KakaoProfile,
    @field:Schema(description = "카카오 이메일")
    @field:JsonProperty("email")
    val email: String,
)
