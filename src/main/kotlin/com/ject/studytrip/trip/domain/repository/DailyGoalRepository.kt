package com.ject.studytrip.trip.domain.repository

import com.ject.studytrip.trip.domain.model.DailyGoal
import java.util.Optional

interface DailyGoalRepository {
    fun save(dailyGoal: DailyGoal): DailyGoal

    fun findById(dailyGoalId: Long): Optional<DailyGoal>
}
