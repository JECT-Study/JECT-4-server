package com.ject.studytrip.pomodoro.application.dto

import com.ject.studytrip.pomodoro.domain.model.Pomodoro

data class PomodoroInfo(
    val pomodoroId: Long,
    val focusDurationInMinute: Int,
    val focusSessionCount: Int,
) {
    companion object {
        @JvmStatic
        fun from(pomodoro: Pomodoro): PomodoroInfo =
            PomodoroInfo(
                pomodoro.getId(),
                pomodoro.getFocusDurationInSeconds() / 60,
                pomodoro.getFocusSessionCount(),
            )
    }
}
