package com.ject.studytrip.stamp.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class CreateStampRequest(
    @field:Schema(description = "스탬프 이름")
    @field:NotBlank(message = "스탬프 이름은 필수 요청 값입니다.")
    val name: String,
    @field:Schema(description = "스탬프 종료일")
    @field:FutureOrPresent(message = "스탬프 종료일은 현재 날짜보다 과거일 수 없습니다.")
    val endDate: LocalDate? = null,
)
