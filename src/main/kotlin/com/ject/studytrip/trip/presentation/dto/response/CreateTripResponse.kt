package com.ject.studytrip.trip.presentation.dto.response

import com.ject.studytrip.trip.application.dto.TripInfo
import io.swagger.v3.oas.annotations.media.Schema

data class CreateTripResponse(
    @field:Schema(description = "여행 ID")
    val tripId: Long,
) {
    companion object {
        fun of(tripInfo: TripInfo): CreateTripResponse = CreateTripResponse(tripInfo.tripId)
    }
}
