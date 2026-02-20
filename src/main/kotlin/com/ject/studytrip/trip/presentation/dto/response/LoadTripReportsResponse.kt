package com.ject.studytrip.trip.presentation.dto.response

import com.ject.studytrip.trip.application.dto.TripReportInfo
import io.swagger.v3.oas.annotations.media.Schema

data class LoadTripReportsResponse(
    val summary: TripReportSummary,
    val tripReports: List<LoadTripReportInfoResponse>,
) {
    companion object {
        fun of(tripReportInfos: List<TripReportInfo>): LoadTripReportsResponse =
            LoadTripReportsResponse(
                TripReportSummary.of(tripReportInfos),
                tripReportInfos.map { LoadTripReportInfoResponse.of(it) },
            )
    }
}

data class TripReportSummary(
    @field:Schema(description = "여행 완료 수")
    val completedTripCount: Long,
    @field:Schema(description = "누적 학습 시간")
    val totalFocusHours: Long,
    @field:Schema(description = "가장 긴 학습 시간")
    val longestFocusHours: Long,
) {
    companion object {
        fun of(tripReportInfos: List<TripReportInfo>): TripReportSummary =
            TripReportSummary(
                tripReportInfos.size.toLong(),
                tripReportInfos.sumOf { it.totalFocusHours },
                tripReportInfos.maxOfOrNull { it.totalFocusHours } ?: 0L,
            )
    }
}

data class LoadTripReportInfoResponse(
    @field:Schema(description = "여행 리포트 ID")
    val tripReportId: Long,
    @field:Schema(description = "여행 리포트 제목")
    val title: String,
    @field:Schema(description = "여행 시작일 (여행 회고)")
    val startDate: String,
    @field:Schema(description = "여행 종료일 (여행 회고)")
    val endDate: String?,
    @field:Schema(description = "총 학습 시간")
    val totalFocusHours: Long,
    @field:Schema(description = "여행 리포트 이미지 URL")
    val imageUrl: String?,
) {
    companion object {
        fun of(tripReportInfo: TripReportInfo): LoadTripReportInfoResponse =
            LoadTripReportInfoResponse(
                tripReportInfo.tripReportId,
                tripReportInfo.title,
                tripReportInfo.startDate,
                tripReportInfo.endDate,
                tripReportInfo.totalFocusHours,
                tripReportInfo.imageUrl,
            )
    }
}
