package com.ject.studytrip.trip.presentation.dto.response

import com.ject.studytrip.studylog.application.dto.StudyLogSliceInfo
import com.ject.studytrip.studylog.presentation.dto.response.LoadStudyLogsSliceResponse
import com.ject.studytrip.trip.application.dto.TripReportInfo
import io.swagger.v3.oas.annotations.media.Schema

data class LoadTripReportDetailResponse(
    @field:Schema(description = "여행 리포트 ID")
    val tripReportId: Long,
    @field:Schema(description = "여행 리포트 제목")
    val title: String,
    @field:Schema(description = "여행 리포트 내용")
    val content: String,
    @field:Schema(description = "여행 시작일 (여행 회고)")
    val startDate: String,
    @field:Schema(description = "여행 종료일 (여행 회고)")
    val endDate: String?,
    @field:Schema(description = "총 학습 시간")
    val totalFocusHours: Long,
    @field:Schema(description = "학습 로그 개수 (세션 성공)")
    val studyLogCount: Long,
    @field:Schema(description = "연속 학습일")
    val studyDays: Long,
    @field:Schema(description = "여행 리포트 이미지 제목")
    val imageTitle: String?,
    @field:Schema(description = "여행 리포트 이미지 URL")
    val imageUrl: String?,
    @field:Schema(description = "학습 로그 히스토리")
    val history: LoadStudyLogsSliceResponse,
) {
    companion object {
        fun of(
            tripReportInfo: TripReportInfo,
            studyLogSliceInfo: StudyLogSliceInfo,
        ): LoadTripReportDetailResponse =
            LoadTripReportDetailResponse(
                tripReportInfo.tripReportId,
                tripReportInfo.title,
                tripReportInfo.content,
                tripReportInfo.startDate,
                tripReportInfo.endDate,
                tripReportInfo.totalFocusHours,
                tripReportInfo.studyLogCount,
                tripReportInfo.studyDays,
                tripReportInfo.imageTitle,
                tripReportInfo.imageUrl,
                LoadStudyLogsSliceResponse.of(studyLogSliceInfo.studyLogDetails, studyLogSliceInfo.hasNext),
            )
    }
}
