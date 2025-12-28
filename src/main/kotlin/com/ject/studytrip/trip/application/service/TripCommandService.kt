package com.ject.studytrip.trip.application.service

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.trip.domain.factory.TripFactory
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.domain.policy.TripPolicy
import com.ject.studytrip.trip.domain.repository.TripCommandRepository
import com.ject.studytrip.trip.domain.repository.TripRepository
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest
import com.ject.studytrip.trip.presentation.dto.request.UpdateTripRequest
import org.springframework.stereotype.Service

@Service
class TripCommandService(
    private val tripRepository: TripRepository,
    private val tripCommandRepository: TripCommandRepository,
) {
    fun createTrip(
        member: Member,
        request: CreateTripRequest,
    ): Trip {
        val category = TripCategory.from(request.category)

        TripPolicy.validateEndDateByCategory(category, request.endDate)

        val trip = TripFactory.create(member, request.name, request.memo, category, request.endDate, request.stamps.size)

        return tripRepository.save(trip)
    }

    fun updateTrip(
        trip: Trip,
        request: UpdateTripRequest,
    ) {
        val category = request.category?.let { TripCategory.from(it) }

        trip.update(request.name, request.memo, category, request.endDate)
    }

    fun deleteTrip(trip: Trip) = trip.updateDeletedAt()

    fun completeTrip(trip: Trip) = trip.updateCompleted()

    fun increaseTotalStamps(trip: Trip) = trip.increaseTotalStamps()

    fun decreaseTotalStamps(trip: Trip) = trip.decreaseTotalStamps()

    fun increaseCompletedStamps(trip: Trip) = trip.increaseCompletedStamps()

    fun hardDeleteTrips(): Long = tripCommandRepository.deleteAllByDeletedAtIsNotNull()

    fun hardDeleteTripsOwnedByDeletedMember(): Long = tripCommandRepository.deleteAllByDeletedMemberOwner()

    fun hardDeleteTripsOwnedByMember(memberId: Long): Long = tripCommandRepository.deleteAllByMemberId(memberId)
}
