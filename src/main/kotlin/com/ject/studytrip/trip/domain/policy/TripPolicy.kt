package com.ject.studytrip.trip.domain.policy

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.trip.domain.error.TripErrorCode
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import java.time.LocalDate

object TripPolicy {
    fun validateNotDeleted(trip: Trip) {
        if (trip.isDeleted) {
            throw CustomException(TripErrorCode.TRIP_ALREADY_DELETED)
        }
    }

    fun validateNotCompleted(trip: Trip) {
        if (trip.isCompleted) {
            throw CustomException(TripErrorCode.TRIP_ALREADY_COMPLETED)
        }
    }

    fun validateCompleted(trip: Trip) {
        if (!trip.isCompleted) {
            throw CustomException(TripErrorCode.TRIP_NOT_COMPLETED)
        }
    }

    fun validateEndDateByCategory(
        category: TripCategory,
        endDate: LocalDate?,
    ) {
        if (category == TripCategory.COURSE && endDate == null) {
            throw CustomException(TripErrorCode.COURSE_TRIP_END_DATE_REQUIRED)
        }
    }

    fun validateOwner(
        memberId: Long,
        trip: Trip,
    ) {
        if (trip.member.id != memberId) {
            throw CustomException(TripErrorCode.NOT_TRIP_OWNER)
        }
    }
}
