package com.ject.studytrip.trip.fixture

import com.ject.studytrip.pomodoro.presentation.dto.request.CreatePomodoroRequest
import com.ject.studytrip.trip.presentation.dto.request.CreateDailyGoalRequest

class CreateDailyGoalRequestFixture {
    var pomodoro: CreatePomodoroRequest = CreatePomodoroRequest(30, 1)
    var missionIds: List<Long> = emptyList()

    fun withPomodoro(pomodoro: CreatePomodoroRequest): CreateDailyGoalRequestFixture = apply { this.pomodoro = pomodoro }

    fun withMissionIds(missionIds: List<Long>): CreateDailyGoalRequestFixture = apply { this.missionIds = missionIds }

    fun build(): CreateDailyGoalRequest = CreateDailyGoalRequest(pomodoro, missionIds)
}
