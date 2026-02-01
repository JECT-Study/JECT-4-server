package com.ject.studytrip.trip.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CreateTripReportRequest(
    @field:Schema(description = "여행 리포트 제목")
    @field:NotEmpty(message = "여행 리포트 제목은 필수 요청 값입니다.")
    val title: String,
    @field:Schema(description = "여행 리포트 내용")
    @field:NotEmpty(message = "여행 리포트 내용은 필수 요청 값입니다.")
    val content: String,
    @field:Schema(description = "여행 시작일")
    @field:NotEmpty(message = "여행 시작일은 필수 요청 값입니다.")
    val startDate: String,
    @field:Schema(description = "여행 종료일")
    val endDate: String?,
    @field:Schema(description = "학습 로그 개수 (세션 성공)")
    val studyLogCount: Long,
    @field:Schema(description = "총 학습 시간")
    val totalFocusHours: Long,
    @field:Schema(description = "연속 학습일")
    val studyDays: Long,
    @field:Schema(description = "이미지 제목")
    val imageTitle: String?,
    @field:Schema(description = "학습 로그 ID 목록")
    @field:NotEmpty(message = "학습 로그 ID 목록은 최소 1개 이상이어야 합니다.")
    val studyLogIds: List<
        @NotNull(message = "학습 로그 ID는 필수 요청 값입니다.")
        Long,
    >,
)
