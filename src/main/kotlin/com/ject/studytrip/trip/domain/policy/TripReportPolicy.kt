package com.ject.studytrip.trip.domain.policy

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.trip.domain.error.TripReportErrorCode
import com.ject.studytrip.trip.domain.model.TripReport

object TripReportPolicy {
    fun validateNotDeleted(tripReport: TripReport) {
        if (tripReport.isDeleted) {
            throw CustomException(TripReportErrorCode.TRIP_REPORT_ALREADY_DELETED)
        }
    }

    fun validateOwner(
        memberId: Long,
        tripReport: TripReport,
    ) {
        if (tripReport.member.id != memberId) {
            throw CustomException(TripReportErrorCode.NOT_TRIP_REPORT_OWNER)
        }
    }
}
