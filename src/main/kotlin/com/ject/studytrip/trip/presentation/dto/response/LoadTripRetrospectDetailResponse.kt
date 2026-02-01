package com.ject.studytrip.trip.presentation.dto.response

import com.ject.studytrip.studylog.application.dto.StudyLogSliceInfo
import com.ject.studytrip.studylog.presentation.dto.response.LoadStudyLogsSliceResponse
import com.ject.studytrip.trip.application.dto.TripInfo
import com.ject.studytrip.trip.application.dto.TripRetrospectSummary
import io.swagger.v3.oas.annotations.media.Schema

data class LoadTripRetrospectDetailResponse(
    @field:Schema(description = "여행 이름")
    val name: String,
    @field:Schema(description = "여행 시작일")
    val startDate: String,
    @field:Schema(description = "여행 종료일")
    val endDate: String?,
    @field:Schema(description = "총 학습 시간")
    val totalFocusHours: Long,
    @field:Schema(description = "학습 로그 개수 (세션 성공)")
    val studyLogCount: Long,
    @field:Schema(description = "연속 학습일")
    val studyDays: Long,
    @field:Schema(description = "학습 로그 ID 목록")
    val studyLogIds: List<Long>,
    @field:Schema(description = "학습 로그 히스토리")
    val history: LoadStudyLogsSliceResponse,
) {
    companion object {
        @JvmStatic
        fun of(
            tripRetrospectSummary: TripRetrospectSummary,
            tripInfo: TripInfo,
            studyLogSliceInfo: StudyLogSliceInfo,
        ): LoadTripRetrospectDetailResponse =
            LoadTripRetrospectDetailResponse(
                tripInfo.tripName,
                tripInfo.startDate,
                tripInfo.endDate,
                tripRetrospectSummary.totalFocusHours,
                tripRetrospectSummary.studyLogCount,
                tripRetrospectSummary.studyDays,
                tripRetrospectSummary.studyLogIds,
                LoadStudyLogsSliceResponse.of(studyLogSliceInfo.studyLogDetails, studyLogSliceInfo.hasNext),
            )
    }
}
