package com.ject.studytrip.trip.domain.factory

import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.Trip

object DailyGoalFactory {
    @JvmStatic
    fun create(
        trip: Trip,
        title: String,
    ): DailyGoal = DailyGoal.of(trip, title)
}
