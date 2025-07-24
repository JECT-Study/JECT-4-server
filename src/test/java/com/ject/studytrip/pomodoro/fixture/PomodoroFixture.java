package com.ject.studytrip.pomodoro.fixture;

import com.ject.studytrip.pomodoro.domain.factory.PomodoroFactory;
import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import org.springframework.test.util.ReflectionTestUtils;

public class PomodoroFixture {
    private static final int FOCUS_DURATION_SECONDS = 30 * 60;
    private static final int FOCUS_COUNT = 1;
    private static final int BREAK_DURATION_SECONDS = 0;

    public static Pomodoro createPomodoro(DailyGoal dailyGoal) {
        return PomodoroFactory.create(
                dailyGoal, FOCUS_DURATION_SECONDS, FOCUS_COUNT, BREAK_DURATION_SECONDS);
    }

    public static Pomodoro createPomodoroWithId(Long id, DailyGoal dailyGoal) {
        Pomodoro pomodoro =
                PomodoroFactory.create(
                        dailyGoal, FOCUS_DURATION_SECONDS, FOCUS_COUNT, BREAK_DURATION_SECONDS);
        ReflectionTestUtils.setField(pomodoro, "id", id);

        return pomodoro;
    }
}
