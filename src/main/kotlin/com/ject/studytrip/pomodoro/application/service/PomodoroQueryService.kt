package com.ject.studytrip.pomodoro.application.service

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.pomodoro.domain.error.PomodoroErrorCode
import com.ject.studytrip.pomodoro.domain.model.Pomodoro
import com.ject.studytrip.pomodoro.domain.policy.PomodoroPolicy
import com.ject.studytrip.pomodoro.domain.repository.PomodoroQueryRepository
import com.ject.studytrip.pomodoro.domain.repository.PomodoroRepository
import org.springframework.stereotype.Service

@Service
class PomodoroQueryService(
    private val pomodoroRepository: PomodoroRepository,
    private val pomodoroQueryRepository: PomodoroQueryRepository,
) {
    fun getValidPomodoroByDailyGoalId(dailyGoalId: Long): Pomodoro {
        val pomodoro =
            pomodoroRepository
                .findByDailyGoalId(dailyGoalId)
                .orElseThrow { CustomException(PomodoroErrorCode.POMODORO_NOT_FOUND) }

        PomodoroPolicy.validateNotDeleted(pomodoro)

        return pomodoro
    }

    fun getTotalFocusHoursByTripId(tripId: Long): Long = pomodoroQueryRepository.sumFocusHoursByTripId(tripId)
}
