package com.ject.studytrip.pomodoro.application.service;

import com.ject.studytrip.pomodoro.domain.factory.PomodoroFactory;
import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import com.ject.studytrip.pomodoro.domain.policy.PomodoroPolicy;
import com.ject.studytrip.pomodoro.domain.repository.PomodoroQueryRepository;
import com.ject.studytrip.pomodoro.domain.repository.PomodoroRepository;
import com.ject.studytrip.pomodoro.presentation.dto.request.CreatePomodoroRequest;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PomodoroCommandService {
    private final PomodoroRepository pomodoroRepository;
    private final PomodoroQueryRepository pomodoroQueryRepository;

    public Pomodoro createPomodoro(DailyGoal dailyGoal, CreatePomodoroRequest request) {
        int focusDurationInSeconds = request.focusDurationInMinute() * 60;

        Pomodoro pomodoro =
                PomodoroFactory.create(
                        dailyGoal, focusDurationInSeconds, request.focusSessionCount(), 0);

        return pomodoroRepository.save(pomodoro);
    }

    public void updateTotalFocusTime(Pomodoro pomodoro, int totalFocusTimeInSeconds) {
        PomodoroPolicy.validateTotalFocusTimeNotNegative(totalFocusTimeInSeconds);

        pomodoro.updateTotalFocusTimeInSeconds(totalFocusTimeInSeconds);
    }

    public void deletePomodoro(Pomodoro pomodoro) {
        pomodoro.updateDeletedAt();
    }

    public long hardDeletePomodoros() {
        return pomodoroQueryRepository.deleteAllByDeletedAtIsNotNull();
    }

    public long hardDeletePomodorosOwnedByDeletedDailyGoal() {
        return pomodoroQueryRepository.deleteAllByDeletedDailyGoalOwner();
    }

    public long hardDeletePomodorosByMember(Long memberId) {
        return pomodoroQueryRepository.deleteAllByMemberId(memberId);
    }
}
