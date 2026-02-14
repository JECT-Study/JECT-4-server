package com.ject.studytrip.member.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

data class ConfirmProfileImageRequest(
    @field:Schema(description = "업로드된 이미지 임시키")
    @field:NotEmpty(message = "업로드된 이미지 임시키는 필수 요청 값입니다.")
    val tmpKey: String,
)
