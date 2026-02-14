package com.ject.studytrip.auth.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class KakaoLoginRequest(
    @field:Schema(description = "카카오 인가 코드")
    @field:NotBlank(message = "카카오 인가 코드를 입력해 주세요.")
    val code: String,
)
