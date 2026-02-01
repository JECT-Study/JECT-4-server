package com.ject.studytrip.trip.application.service

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.trip.domain.error.TripReportErrorCode
import com.ject.studytrip.trip.domain.model.TripReport
import com.ject.studytrip.trip.domain.policy.TripReportPolicy
import com.ject.studytrip.trip.domain.repository.TripReportQueryRepository
import com.ject.studytrip.trip.domain.repository.TripReportRepository
import org.springframework.stereotype.Service

@Service
class TripReportQueryService(
    private val tripReportRepository: TripReportRepository,
    private val tripReportQueryRepository: TripReportQueryRepository,
) {
    fun getTripReport(tripReportId: Long): TripReport =
        tripReportRepository
            .findById(tripReportId)
            .orElseThrow { CustomException(TripReportErrorCode.TRIP_REPORT_NOT_FOUND) }

    fun getValidTripReport(
        memberId: Long,
        tripReportId: Long,
    ): TripReport {
        val tripReport =
            tripReportRepository
                .findById(tripReportId)
                .orElseThrow { CustomException(TripReportErrorCode.TRIP_REPORT_NOT_FOUND) }

        TripReportPolicy.validateOwner(memberId, tripReport)
        TripReportPolicy.validateNotDeleted(tripReport)

        return tripReport
    }

    fun getTripReportsByMemberId(memberId: Long): List<TripReport> = tripReportQueryRepository.findAllActiveByMemberId(memberId)

    fun getTripReportImageUrlsByMemberId(memberId: Long): List<String> = tripReportQueryRepository.findImageUrlsByMemberId(memberId)
}
