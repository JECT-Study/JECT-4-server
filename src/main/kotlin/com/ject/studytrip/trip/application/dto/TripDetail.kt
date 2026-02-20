package com.ject.studytrip.trip.application.dto

import com.ject.studytrip.stamp.application.dto.StampInfo

data class TripDetail(
    val tripInfo: TripInfo,
    val stampInfos: List<StampInfo>,
)
