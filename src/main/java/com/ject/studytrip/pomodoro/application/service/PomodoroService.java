package com.ject.studytrip.pomodoro.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.pomodoro.domain.error.PomodoroErrorCode;
import com.ject.studytrip.pomodoro.domain.factory.PomodoroFactory;
import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import com.ject.studytrip.pomodoro.domain.policy.PomodoroPolicy;
import com.ject.studytrip.pomodoro.domain.repository.PomodoroQueryRepository;
import com.ject.studytrip.pomodoro.domain.repository.PomodoroRepository;
import com.ject.studytrip.pomodoro.presentation.dto.request.CreatePomodoroRequest;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PomodoroService {
    private final PomodoroRepository pomodoroRepository;
    private final PomodoroQueryRepository pomodoroQueryRepository;

    public Pomodoro createPomodoro(DailyGoal dailyGoal, CreatePomodoroRequest request) {
        int focusDurationInSeconds = request.focusDurationInMinute() * 60;
        Pomodoro pomodoro =
                PomodoroFactory.create(
                        dailyGoal, focusDurationInSeconds, request.focusSessionCount(), 0);
        return pomodoroRepository.save(pomodoro);
    }

    public void deletePomodoro(Pomodoro pomodoro) {
        pomodoro.updateDeletedAt();
    }

    public Pomodoro getValidPomodoroByDailyGoal(Long dailyGoalId) {
        Pomodoro pomodoro =
                pomodoroRepository
                        .findByDailyGoalId(dailyGoalId)
                        .orElseThrow(
                                () -> new CustomException(PomodoroErrorCode.POMODORO_NOT_FOUND));

        PomodoroPolicy.validateNotDeleted(pomodoro);

        return pomodoro;
    }

    public void updateTotalFocusTime(Long dailyGoalId, int totalFocusTimeInSeconds) {
        PomodoroPolicy.validateTotalFocusTimeNotNegative(totalFocusTimeInSeconds);

        Pomodoro pomodoro =
                pomodoroRepository
                        .findByDailyGoalId(dailyGoalId)
                        .orElseThrow(
                                () -> new CustomException(PomodoroErrorCode.POMODORO_NOT_FOUND));
        PomodoroPolicy.validateNotDeleted(pomodoro);

        pomodoro.updateTotalFocusTimeInSeconds(totalFocusTimeInSeconds);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long hardDeletePomodoros() {
        return pomodoroQueryRepository.deleteAllByDeletedAtIsNotNull();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long hardDeletePomodorosOwnedByDeletedDailyGoal() {
        return pomodoroQueryRepository.deleteAllByDeletedDailyGoalOwner();
    }
}
