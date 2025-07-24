package com.ject.studytrip.pomodoro.domain.policy;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.pomodoro.domain.error.PomodoroErrorCode;
import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PomodoroPolicy {
    public static void validateNotDeleted(Pomodoro pomodoro) {
        if (pomodoro.getDeletedAt() != null)
            throw new CustomException(PomodoroErrorCode.POMODORO_ALREADY_DELETED);
    }
}
