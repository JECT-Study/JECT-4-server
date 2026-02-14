package com.ject.studytrip.auth.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class LogoutRequest(
    @field:Schema(description = "엑세스 토큰")
    @field:NotBlank(message = "엑세스 토큰을 입력해 주세요.")
    val accessToken: String,
)
