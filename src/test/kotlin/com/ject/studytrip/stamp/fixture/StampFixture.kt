package com.ject.studytrip.stamp.fixture

import com.ject.studytrip.stamp.domain.factory.StampFactory
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.trip.domain.model.Trip
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDate

class StampFixture(
    private val trip: Trip,
    private val order: Int,
) {
    var name: String = "TEST 스탬프 이름"
    var endTime: LocalDate = LocalDate.now().plusDays(7)

    fun create(): Stamp = StampFactory.create(trip, name, order, endTime)

    fun createWithId(id: Long): Stamp =
        create().also {
            ReflectionTestUtils.setField(it, "id", id)
        }
}
