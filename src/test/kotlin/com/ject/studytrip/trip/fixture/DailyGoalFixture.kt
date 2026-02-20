package com.ject.studytrip.trip.fixture

import com.ject.studytrip.trip.domain.factory.DailyGoalFactory
import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.Trip
import org.springframework.test.util.ReflectionTestUtils

class DailyGoalFixture(
    private val trip: Trip,
    private val title: String = "TEST 데일리 목표 제목",
) {
    fun create(): DailyGoal = DailyGoalFactory.create(trip, title)

    fun createWithId(id: Long): DailyGoal =
        create().also {
            ReflectionTestUtils.setField(it, "id", id)
        }
}
