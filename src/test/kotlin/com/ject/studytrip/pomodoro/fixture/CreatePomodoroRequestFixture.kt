package com.ject.studytrip.pomodoro.fixture

import com.ject.studytrip.pomodoro.presentation.dto.request.CreatePomodoroRequest

class CreatePomodoroRequestFixture(
    private val focusDurationInMinute: Int = 25,
    private val focusSessionCount: Int = 4,
) {
    fun withFocusDurationInMinute(focusDurationInMinute: Int): CreatePomodoroRequestFixture =
        CreatePomodoroRequestFixture(focusDurationInMinute, focusSessionCount)

    fun withFocusSessionCount(focusSessionCount: Int): CreatePomodoroRequestFixture =
        CreatePomodoroRequestFixture(focusDurationInMinute, focusSessionCount)

    fun build(): CreatePomodoroRequest = CreatePomodoroRequest(focusDurationInMinute, focusSessionCount)
}
