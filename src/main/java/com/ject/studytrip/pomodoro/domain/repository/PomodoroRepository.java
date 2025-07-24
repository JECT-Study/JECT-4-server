package com.ject.studytrip.pomodoro.domain.repository;

import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import java.util.Optional;

public interface PomodoroRepository {
    Pomodoro save(Pomodoro pomodoro);

    Optional<Pomodoro> findByDailyGoalId(Long dailyGoalId);
}
