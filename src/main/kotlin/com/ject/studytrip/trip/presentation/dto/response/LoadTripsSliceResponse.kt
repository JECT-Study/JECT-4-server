package com.ject.studytrip.trip.presentation.dto.response

import com.ject.studytrip.trip.application.dto.TripInfo
import io.swagger.v3.oas.annotations.media.Schema

data class LoadTripsSliceResponse(
    @field:Schema(description = "여행 목록")
    val tripInfos: List<TripInfo>,
    @field:Schema(description = "다음 데이터 존재 여부")
    val hasNext: Boolean,
) {
    companion object {
        @JvmStatic
        fun of(
            tripInfos: List<TripInfo>,
            hasNext: Boolean,
        ): LoadTripsSliceResponse = LoadTripsSliceResponse(tripInfos, hasNext)
    }
}
