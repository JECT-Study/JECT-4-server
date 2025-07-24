package com.ject.studytrip.pomodoro.application.dto;

import com.ject.studytrip.pomodoro.domain.model.Pomodoro;

public record PomodoroInfo(Long pomodoroId, int focusDurationInMinute) {
    public static PomodoroInfo from(Pomodoro pomodoro) {
        return new PomodoroInfo(pomodoro.getId(), pomodoro.getFocusDurationInSeconds() / 60);
    }
}
