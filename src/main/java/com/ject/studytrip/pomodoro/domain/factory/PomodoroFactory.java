package com.ject.studytrip.pomodoro.domain.factory;

import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PomodoroFactory {
    public static Pomodoro create(
            DailyGoal dailyGoal,
            int focusDurationInSeconds,
            int focusSessionCount,
            int breakDurationInSeconds) {
        return Pomodoro.of(
                dailyGoal, focusDurationInSeconds, focusSessionCount, breakDurationInSeconds);
    }
}
