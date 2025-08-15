package com.ject.studytrip.pomodoro.presentation.dto.request;

import jakarta.validation.constraints.Min;

public record CreatePomodoroRequest(
        @Min(value = 1, message = "뽀모도로 최소 집중 시간은 1분입니다.") int focusDurationInMinute,
        @Min(value = 1, message = "뽀모도로 최소 집중 세션 개수는 1개입니다.") int focusSessionCount) {}
