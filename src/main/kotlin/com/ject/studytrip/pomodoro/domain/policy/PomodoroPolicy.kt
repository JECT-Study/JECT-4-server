package com.ject.studytrip.pomodoro.domain.policy

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.pomodoro.domain.error.PomodoroErrorCode
import com.ject.studytrip.pomodoro.domain.model.Pomodoro

object PomodoroPolicy {
    fun validateNotDeleted(pomodoro: Pomodoro) {
        if (pomodoro.isDeleted) {
            throw CustomException(PomodoroErrorCode.POMODORO_ALREADY_DELETED)
        }
    }

    fun validateTotalFocusTimeNotNegative(totalFocusTime: Int) {
        if (totalFocusTime < 0) {
            throw CustomException(PomodoroErrorCode.POMODORO_NEGATIVE_FOCUS_TIME)
        }
    }
}
