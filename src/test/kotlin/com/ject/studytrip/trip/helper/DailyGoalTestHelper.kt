package com.ject.studytrip.trip.helper

import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.repository.DailyGoalRepository
import com.ject.studytrip.trip.fixture.DailyGoalFixture
import org.springframework.stereotype.Component

@Component
class DailyGoalTestHelper(
    private val dailyGoalRepository: DailyGoalRepository,
) {
    fun saveDailyGoal(trip: Trip): DailyGoal = dailyGoalRepository.save(DailyGoalFixture(trip).create())

    fun saveDeletedDailyGoal(trip: Trip): DailyGoal =
        dailyGoalRepository.save(DailyGoalFixture(trip).create().also { it.updateDeletedAt() })
}
