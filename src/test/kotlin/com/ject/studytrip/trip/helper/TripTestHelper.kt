package com.ject.studytrip.trip.helper

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.domain.repository.TripRepository
import com.ject.studytrip.trip.fixture.TripFixture
import org.springframework.stereotype.Component

@Component
class TripTestHelper(
    private val tripRepository: TripRepository,
) {
    fun saveTrip(
        member: Member,
        category: TripCategory,
    ): Trip = tripRepository.save(TripFixture(member, category).create())

    fun saveDeletedTrip(
        member: Member,
        category: TripCategory,
    ): Trip = tripRepository.save(TripFixture(member, category).create().also { it.updateDeletedAt() })

    fun saveCompletedTrip(
        member: Member,
        category: TripCategory,
    ): Trip = tripRepository.save(TripFixture(member, category).create().also { it.updateCompleted() })
}
