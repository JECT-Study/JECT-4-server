package com.ject.studytrip.trip.application.dto

data class TripSliceInfo(
    val tripInfos: List<TripInfo>,
    val hasNext: Boolean,
)
