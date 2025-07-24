package com.ject.studytrip.pomodoro.helper;

import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import com.ject.studytrip.pomodoro.domain.repository.PomodoroRepository;
import com.ject.studytrip.pomodoro.fixture.PomodoroFixture;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PomodoroTestHelper {

    @Autowired private PomodoroRepository pomodoroRepository;

    public Pomodoro savePomodoro(DailyGoal dailyGoal) {
        return pomodoroRepository.save(PomodoroFixture.createPomodoro(dailyGoal));
    }

    public Pomodoro saveDeletedPomodoro(DailyGoal dailyGoal) {
        Pomodoro pomodoro = pomodoroRepository.save(PomodoroFixture.createPomodoro(dailyGoal));
        pomodoro.updateDeletedAt();

        return pomodoro;
    }
}
