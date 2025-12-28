package com.ject.studytrip.stamp.domain.policy

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.stamp.domain.error.StampErrorCode
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.trip.domain.model.TripCategory
import java.time.LocalDate

object StampPolicy {
    fun validateNotDeleted(stamp: Stamp) {
        if (stamp.isDeleted) {
            throw CustomException(StampErrorCode.STAMP_ALREADY_DELETED)
        }
    }

    fun validateNotCompleted(stamp: Stamp) {
        if (stamp.isCompleted) {
            throw CustomException(StampErrorCode.STAMP_ALREADY_COMPLETED)
        }
    }

    fun validateNotAllCompleted(exists: Boolean) {
        if (exists) {
            throw CustomException(StampErrorCode.ALL_STAMPS_NOT_COMPLETED)
        }
    }

    fun validateNotStampListEmpty(stamps: List<Stamp>) {
        if (stamps.isEmpty()) {
            throw CustomException(StampErrorCode.STAMP_LIST_NOT_EMPTY)
        }
    }

    fun validateNotStampEndDateAfterTripEndDate(
        tripEndDate: LocalDate?,
        stampEndDate: LocalDate?,
    ) {
        if (tripEndDate == null || stampEndDate == null) return

        // 스탬프 종료일이 여행 종료일 이후일 경우
        if (stampEndDate.isAfter(tripEndDate)) {
            throw CustomException(StampErrorCode.STAMP_END_DATE_AFTER_TRIP_END_DATE_NOT_ALLOWED)
        }
    }

    fun validateUpdateStampOrders(
        tripCategory: TripCategory,
        orderedStampIds: List<Long>,
        savedStamps: List<Stamp>,
    ) {
        if (tripCategory == TripCategory.EXPLORE && orderedStampIds.isNotEmpty()) {
            throw CustomException(StampErrorCode.CANNOT_UPDATE_ORDER_FOR_EXPLORATION_TRIP)
        }

        if (orderedStampIds.size != savedStamps.size) {
            throw CustomException(StampErrorCode.INVALID_STAMP_ID_IN_REQUEST)
        }
    }

    fun validateStampBelongsToTrip(
        tripId: Long,
        stamp: Stamp,
    ) {
        if (stamp.trip.id != tripId) {
            throw CustomException(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP)
        }
    }
}
