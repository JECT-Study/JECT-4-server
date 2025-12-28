package com.ject.studytrip.trip.presentation.dto.request

import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

data class CreateTripRequest(
    @field:Schema(description = "여행 이름")
    @field:NotEmpty(message = "여행 이름은 필수 요청 값입니다.")
    val name: String,
    @field:Schema(description = "여행 메모")
    val memo: String?,
    @field:Schema(description = "여행 카테고리")
    @field:Pattern(
        regexp = "^(COURSE|EXPLORE)$",
        message = "여행 카테고리는 COURSE, EXPLORE 중 하나여야 합니다.",
    )
    val category: String,
    @field:Schema(description = "여행 종료일")
    @field:FutureOrPresent(message = "여행 종료일은 현재 날짜보다 과거일 수 없습니다.")
    val endDate: LocalDate?,
    @field:Schema(description = "여행 스탬프 목록")
    @field:Valid
    @field:NotEmpty(message = "스탬프는 최소 1개 이상 함께 등록해야 합니다.")
    val stamps: List<CreateStampRequest>,
)
