package com.ject.studytrip.trip.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class UpdateTripRequest(
    @field:Schema(description = "수정할 여행 이름")
    @field:Size(min = 1, message = "여행 이름은 최소 1글자 이상이여야 합니다.")
    val name: String?,
    @field:Schema(description = "수정할 여행 메모")
    val memo: String?,
    @field:Schema(description = "수정할 여행 카테고리")
    @field:Pattern(
        regexp = "^(COURSE|EXPLORE)$",
        message = "여행 카테고리는 COURSE, EXPLORE 중 하나여야 합니다.",
    )
    val category: String?,
    @field:Schema(description = "수정할 여행 종료일")
    @field:FutureOrPresent(message = "여행 종료일은 현재 날짜보다 과거일 수 없습니다.")
    val endDate: LocalDate?,
)
