package com.ject.studytrip.pomodoro.domain.repository

interface PomodoroQueryRepository {
    fun sumFocusHoursByTripId(tripId: Long): Long
}
