package com.ject.studytrip.trip.fixture

import com.ject.studytrip.pomodoro.presentation.dto.request.CreatePomodoroRequest
import com.ject.studytrip.trip.presentation.dto.request.CreateDailyGoalRequest

class CreateDailyGoalRequestFixture(
    private val pomodoro: CreatePomodoroRequest = CreatePomodoroRequest(30, 1),
    private val missionIds: List<Long> = emptyList(),
) {
    fun withPomodoro(pomodoro: CreatePomodoroRequest): CreateDailyGoalRequestFixture = CreateDailyGoalRequestFixture(pomodoro, missionIds)

    fun withMissionIds(missionIds: List<Long>): CreateDailyGoalRequestFixture = CreateDailyGoalRequestFixture(pomodoro, missionIds)

    fun build(): CreateDailyGoalRequest = CreateDailyGoalRequest(pomodoro, missionIds)
}
