package com.ject.studytrip.pomodoro.helper

import com.ject.studytrip.pomodoro.domain.model.Pomodoro
import com.ject.studytrip.pomodoro.domain.repository.PomodoroRepository
import com.ject.studytrip.pomodoro.fixture.PomodoroFixture
import com.ject.studytrip.trip.domain.model.DailyGoal
import org.springframework.stereotype.Component

@Component
class PomodoroTestHelper(
    private val pomodoroRepository: PomodoroRepository,
) {
    fun savePomodoro(dailyGoal: DailyGoal): Pomodoro = pomodoroRepository.save(PomodoroFixture(dailyGoal).create())

    fun saveDeletedPomodoro(dailyGoal: DailyGoal): Pomodoro =
        pomodoroRepository.save(PomodoroFixture(dailyGoal).create().also { it.updateDeletedAt() })
}
