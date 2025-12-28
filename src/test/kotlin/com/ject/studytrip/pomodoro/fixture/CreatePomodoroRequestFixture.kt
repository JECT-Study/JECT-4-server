package com.ject.studytrip.pomodoro.fixture

import com.ject.studytrip.pomodoro.presentation.dto.request.CreatePomodoroRequest

class CreatePomodoroRequestFixture {
    var focusDurationInMinute: Int = 25
    var focusSessionCount: Int = 4

    fun withFocusDurationInMinute(focusDurationInMinute: Int): CreatePomodoroRequestFixture =
        apply { this.focusDurationInMinute = focusDurationInMinute }

    fun withFocusSessionCount(focusSessionCount: Int): CreatePomodoroRequestFixture = apply { this.focusSessionCount = focusSessionCount }

    fun build(): CreatePomodoroRequest = CreatePomodoroRequest(focusDurationInMinute, focusSessionCount)
}
