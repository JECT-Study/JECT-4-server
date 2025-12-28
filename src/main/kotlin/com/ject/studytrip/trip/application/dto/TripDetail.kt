package com.ject.studytrip.trip.application.dto

import com.ject.studytrip.stamp.application.dto.StampInfo

data class TripDetail(
    val tripInfo: TripInfo,
    val stampInfos: List<StampInfo>,
) {
    companion object {
        @JvmStatic
        fun from(
            tripInfo: TripInfo,
            stampInfos: List<StampInfo>,
        ): TripDetail = TripDetail(tripInfo, stampInfos)
    }
}
