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
    fun savePomodoro(dailyGoal: DailyGoal): Pomodoro {
        val pomodoro = PomodoroFixture(dailyGoal).create()
        return pomodoroRepository.save(pomodoro)
    }

    fun saveDeletedPomodoro(dailyGoal: DailyGoal): Pomodoro {
        val pomodoro =
            PomodoroFixture(dailyGoal).create().also {
                it.updateDeletedAt()
            }
        return pomodoroRepository.save(pomodoro)
    }
}
