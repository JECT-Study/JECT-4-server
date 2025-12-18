package com.ject.studytrip.pomodoro.application.service

import com.ject.studytrip.pomodoro.domain.factory.PomodoroFactory
import com.ject.studytrip.pomodoro.domain.model.Pomodoro
import com.ject.studytrip.pomodoro.domain.policy.PomodoroPolicy
import com.ject.studytrip.pomodoro.domain.repository.PomodoroCommandRepository
import com.ject.studytrip.pomodoro.domain.repository.PomodoroRepository
import com.ject.studytrip.pomodoro.presentation.dto.request.CreatePomodoroRequest
import com.ject.studytrip.trip.domain.model.DailyGoal
import org.springframework.stereotype.Service

@Service
class PomodoroCommandService(
    private val pomodoroRepository: PomodoroRepository,
    private val pomodoroCommandRepository: PomodoroCommandRepository,
) {
    fun createPomodoro(
        dailyGoal: DailyGoal,
        request: CreatePomodoroRequest,
    ): Pomodoro {
        val focusDurationInSeconds = request.focusDurationInMinute * 60

        val pomodoro =
            PomodoroFactory.create(
                dailyGoal,
                focusDurationInSeconds,
                request.focusSessionCount,
                0,
            )

        return pomodoroRepository.save(pomodoro)
    }

    fun updateTotalFocusTime(
        pomodoro: Pomodoro,
        totalFocusTimeInSeconds: Int,
    ) {
        PomodoroPolicy.validateNotDeleted(pomodoro)
        PomodoroPolicy.validateTotalFocusTimeNotNegative(totalFocusTimeInSeconds)

        pomodoro.updateTotalFocusTimeInSeconds(totalFocusTimeInSeconds)
    }

    fun deletePomodoro(pomodoro: Pomodoro) {
        PomodoroPolicy.validateNotDeleted(pomodoro)

        pomodoro.updateDeletedAt()
    }

    fun hardDeletePomodoros(): Long = pomodoroCommandRepository.deleteAllByDeletedAtIsNotNull()

    fun hardDeletePomodorosOwnedByDeletedDailyGoal(): Long = pomodoroCommandRepository.deleteAllByDeletedDailyGoalOwner()

    fun hardDeletePomodorosOwnedByMember(memberId: Long): Long = pomodoroCommandRepository.deleteAllByMemberId(memberId)
}
