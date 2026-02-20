package com.ject.studytrip.pomodoro.application.dto

import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.pomodoro.domain.model.Pomodoro

data class PomodoroInfo(
    val pomodoroId: Long,
    val focusDurationInMinute: Int,
    val focusSessionCount: Int,
) {
    companion object {
        fun from(pomodoro: Pomodoro): PomodoroInfo =
            PomodoroInfo(
                pomodoro.id.requireId(),
                pomodoro.focusDurationInSeconds / 60,
                pomodoro.focusSessionCount,
            )
    }
}
