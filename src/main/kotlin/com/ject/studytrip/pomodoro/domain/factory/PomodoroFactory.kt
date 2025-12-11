package com.ject.studytrip.pomodoro.domain.factory

import com.ject.studytrip.pomodoro.domain.model.Pomodoro
import com.ject.studytrip.trip.domain.model.DailyGoal

object PomodoroFactory {
    fun create(
        dailyGoal: DailyGoal,
        focusDurationInSeconds: Int,
        focusSessionCount: Int,
        breakDurationInSeconds: Int,
    ): Pomodoro = Pomodoro.of(dailyGoal, focusDurationInSeconds, focusSessionCount, breakDurationInSeconds)
}
