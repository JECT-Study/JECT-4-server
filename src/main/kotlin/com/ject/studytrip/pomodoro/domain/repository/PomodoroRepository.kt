package com.ject.studytrip.pomodoro.domain.repository

import com.ject.studytrip.pomodoro.domain.model.Pomodoro
import java.util.Optional

interface PomodoroRepository {
    fun save(pomodoro: Pomodoro): Pomodoro

    fun findByDailyGoalId(dailyGoalId: Long): Optional<Pomodoro>
}
