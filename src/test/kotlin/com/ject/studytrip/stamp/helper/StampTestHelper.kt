package com.ject.studytrip.stamp.helper

import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.domain.repository.StampRepository
import com.ject.studytrip.stamp.fixture.StampFixture
import com.ject.studytrip.trip.domain.model.Trip
import org.springframework.stereotype.Component

@Component
class StampTestHelper(
    private val stampRepository: StampRepository,
) {
    fun saveStamp(
        trip: Trip,
        order: Int,
    ): Stamp = stampRepository.save(StampFixture(trip, order).create())

    fun saveDeletedStamp(
        trip: Trip,
        order: Int,
    ): Stamp = stampRepository.save(StampFixture(trip, order).create().also { it.updateDeletedAt() })

    fun saveCompletedStamp(
        trip: Trip,
        order: Int,
    ): Stamp = stampRepository.save(StampFixture(trip, order).create().also { it.updateCompleted() })

    fun getStamp(stampId: Long): Stamp = stampRepository.findById(stampId).orElseThrow()
}
