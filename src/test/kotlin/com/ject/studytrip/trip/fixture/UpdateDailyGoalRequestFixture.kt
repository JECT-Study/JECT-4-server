package com.ject.studytrip.trip.fixture

import com.ject.studytrip.trip.presentation.dto.request.UpdateDailyGoalRequest

class UpdateDailyGoalRequestFixture(
    private val deleteDailyMissionIds: List<Long> = emptyList(),
    private val addMissionIds: List<Long> = emptyList(),
) {
    fun withDeleteDailyMissionIds(deleteDailyMissionIds: List<Long>): UpdateDailyGoalRequestFixture =
        UpdateDailyGoalRequestFixture(deleteDailyMissionIds, addMissionIds)

    fun withAddMissionIds(addMissionIds: List<Long>): UpdateDailyGoalRequestFixture =
        UpdateDailyGoalRequestFixture(deleteDailyMissionIds, addMissionIds)

    fun build(): UpdateDailyGoalRequest = UpdateDailyGoalRequest(deleteDailyMissionIds, addMissionIds)
}
