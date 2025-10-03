package com.ject.studytrip.pomodoro.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.pomodoro.domain.error.PomodoroErrorCode;
import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import com.ject.studytrip.pomodoro.domain.policy.PomodoroPolicy;
import com.ject.studytrip.pomodoro.domain.repository.PomodoroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PomodoroQueryService {
    private final PomodoroRepository pomodoroRepository;

    public Pomodoro getValidPomodoroByDailyGoal(Long dailyGoalId) {
        Pomodoro pomodoro =
                pomodoroRepository
                        .findByDailyGoalId(dailyGoalId)
                        .orElseThrow(
                                () -> new CustomException(PomodoroErrorCode.POMODORO_NOT_FOUND));

        PomodoroPolicy.validateNotDeleted(pomodoro);

        return pomodoro;
    }
}
