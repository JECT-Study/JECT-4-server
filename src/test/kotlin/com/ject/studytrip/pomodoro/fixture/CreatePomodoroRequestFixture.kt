package com.ject.studytrip.pomodoro.fixture

import com.ject.studytrip.pomodoro.presentation.dto.request.CreatePomodoroRequest

class CreatePomodoroRequestFixture {
    var focusDurationInMinute: Int = 25
    var focusSessionCount: Int = 4

    fun build(): CreatePomodoroRequest = CreatePomodoroRequest(focusDurationInMinute, focusSessionCount)
}
