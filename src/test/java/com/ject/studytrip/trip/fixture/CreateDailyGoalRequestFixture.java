package com.ject.studytrip.trip.fixture;

import com.ject.studytrip.pomodoro.presentation.dto.request.CreatePomodoroRequest;
import com.ject.studytrip.trip.presentation.dto.request.CreateDailyGoalRequest;
import java.util.List;

public class CreateDailyGoalRequestFixture {

    private CreatePomodoroRequest pomodoro = new CreatePomodoroRequest(30);
    private List<Long> missionIds = List.of(1L, 2L);

    public CreateDailyGoalRequestFixture withPomodoro(CreatePomodoroRequest pomodoro) {
        this.pomodoro = pomodoro;
        return this;
    }

    public CreateDailyGoalRequestFixture withMissionIds(List<Long> missionIds) {
        this.missionIds = missionIds;
        return this;
    }

    public CreateDailyGoalRequest build() {
        return new CreateDailyGoalRequest(pomodoro, missionIds);
    }
}
