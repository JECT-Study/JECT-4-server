package com.ject.studytrip.trip.application.dto

import com.ject.studytrip.global.util.DateUtil
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory

data class TripInfo(
    val tripId: Long,
    val tripName: String,
    val tripMemo: String,
    val tripCategory: TripCategory,
    val startDate: String,
    val endDate: String?,
    val dDay: Int?,
    val totalStamps: Int,
    val completedStamps: Int,
    val progress: Int?,
    val completed: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
) {
    companion object {
        @JvmStatic
        fun from(
            trip: Trip,
            dDay: Int?,
            progress: Int?,
        ): TripInfo =
            TripInfo(
                trip.id,
                trip.name,
                trip.memo,
                trip.category,
                DateUtil.formatDate(trip.startDate),
                trip.endDate?.let { DateUtil.formatDate(it) },
                dDay,
                trip.totalStamps,
                trip.completedStamps,
                progress,
                trip.isCompleted,
                DateUtil.formatDateTime(trip.createdAt),
                DateUtil.formatDateTime(trip.updatedAt),
                trip.deletedAt?.let { DateUtil.formatDateTime(it) },
            )
    }
}
