package com.ject.studytrip.trip.presentation.dto.response

import com.ject.studytrip.stamp.application.dto.StampInfo
import com.ject.studytrip.stamp.presentation.dto.response.LoadStampInfoResponse
import com.ject.studytrip.trip.application.dto.TripInfo
import com.ject.studytrip.trip.domain.model.TripCategory
import io.swagger.v3.oas.annotations.media.Schema

data class LoadTripDetailResponse(
    @field:Schema(description = "여행 ID")
    val tripId: Long,
    @field:Schema(description = "여행 이름")
    val name: String,
    @field:Schema(description = "여행 메모")
    val memo: String,
    @field:Schema(description = "여행 카테고리")
    val category: TripCategory,
    @field:Schema(description = "여행 시작일")
    val startDate: String,
    @field:Schema(description = "여행 종료일")
    val endDate: String?,
    @field:Schema(description = "D-DAY")
    val dDay: Int?,
    @field:Schema(description = "여행의 총 스탬프 수")
    val totalStamps: Int,
    @field:Schema(description = "완료된 총 스탬프 수")
    val completedStamps: Int,
    @field:Schema(description = "진행률")
    val progress: Int?,
    @field:Schema(description = "여행 완료 여부")
    val completed: Boolean,
    @field:Schema(description = "여행에 속한 스탬프 목록")
    val stamps: List<LoadStampInfoResponse>,
) {
    companion object {
        fun of(
            tripInfo: TripInfo,
            stampInfos: List<StampInfo>,
        ): LoadTripDetailResponse =
            LoadTripDetailResponse(
                tripInfo.tripId,
                tripInfo.tripName,
                tripInfo.tripMemo,
                tripInfo.tripCategory,
                tripInfo.startDate,
                tripInfo.endDate,
                tripInfo.dDay,
                tripInfo.totalStamps,
                tripInfo.completedStamps,
                tripInfo.progress,
                tripInfo.completed,
                stampInfos.map(LoadStampInfoResponse::of),
            )
    }
}
