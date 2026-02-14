package com.ject.studytrip.member.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

data class PresignProfileImageRequest(
    @field:Schema(description = "원본 이미지 파일명")
    @field:NotEmpty(message = "원본 이미지 파일명은 필수 요청 값입니다.")
    val originFilename: String,
)
