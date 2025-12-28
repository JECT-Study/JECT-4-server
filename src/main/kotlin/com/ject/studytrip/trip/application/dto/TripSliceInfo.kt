package com.ject.studytrip.trip.application.dto

data class TripSliceInfo(
    val tripInfos: List<TripInfo>,
    val hasNext: Boolean,
) {
    companion object {
        @JvmStatic
        fun of(
            tripInfos: List<TripInfo>,
            hasNext: Boolean,
        ): TripSliceInfo = TripSliceInfo(tripInfos, hasNext)
    }
}
