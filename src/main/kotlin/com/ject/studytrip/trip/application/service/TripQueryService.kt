package com.ject.studytrip.trip.application.service

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.trip.application.dto.TripCount
import com.ject.studytrip.trip.domain.error.TripErrorCode
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.domain.policy.TripPolicy
import com.ject.studytrip.trip.domain.repository.TripQueryRepository
import com.ject.studytrip.trip.domain.repository.TripRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service

@Service
class TripQueryService(
    private val tripRepository: TripRepository,
    private val tripQueryRepository: TripQueryRepository,
) {
    fun getValidTrip(
        memberId: Long,
        tripId: Long,
    ): Trip {
        val trip =
            tripRepository
                .findById(tripId)
                .orElseThrow { CustomException(TripErrorCode.TRIP_NOT_FOUND) }

        TripPolicy.validateOwner(memberId, trip)
        TripPolicy.validateNotDeleted(trip)
        TripPolicy.validateNotCompleted(trip)

        return trip
    }

    fun getTripsSliceByMemberId(
        memberId: Long,
        page: Int,
        size: Int,
    ): Slice<Trip> = tripQueryRepository.findSliceByMemberIdAndCompletedFalseAndDeletedAtIsNull(memberId, PageRequest.of(page, size))

    fun getActiveTripCountByMemberId(memberId: Long): TripCount {
        val courseCount = tripQueryRepository.countActiveTripsByMemberIdAndCategory(memberId, TripCategory.COURSE)
        val exploreCount = tripQueryRepository.countActiveTripsByMemberIdAndCategory(memberId, TripCategory.EXPLORE)

        return TripCount(courseCount, exploreCount)
    }

    fun getValidCompletedTrip(
        memberId: Long,
        tripId: Long,
    ): Trip {
        val trip =
            tripRepository
                .findById(tripId)
                .orElseThrow { CustomException(TripErrorCode.TRIP_NOT_FOUND) }

        TripPolicy.validateOwner(memberId, trip)
        TripPolicy.validateNotDeleted(trip)
        TripPolicy.validateCompleted(trip)

        return trip
    }
}
