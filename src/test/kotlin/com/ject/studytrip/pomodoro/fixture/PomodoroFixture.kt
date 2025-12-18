package com.ject.studytrip.pomodoro.fixture

import com.ject.studytrip.pomodoro.domain.factory.PomodoroFactory
import com.ject.studytrip.pomodoro.domain.model.Pomodoro
import com.ject.studytrip.trip.domain.model.DailyGoal
import org.springframework.test.util.ReflectionTestUtils

class PomodoroFixture(
    private val dailyGoal: DailyGoal,
) {
    var focusDurationSeconds: Int = 30 * 60
    var focusSessionCount: Int = 1
    var breakDurationSeconds: Int = 0

    fun create(): Pomodoro = PomodoroFactory.create(dailyGoal, focusDurationSeconds, focusSessionCount, breakDurationSeconds)

    fun createWithId(id: Long): Pomodoro =
        create().also {
            ReflectionTestUtils.setField(it, "id", id)
        }
}
