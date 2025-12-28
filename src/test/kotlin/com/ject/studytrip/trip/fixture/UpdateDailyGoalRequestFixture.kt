package com.ject.studytrip.trip.fixture

import com.ject.studytrip.trip.presentation.dto.request.UpdateDailyGoalRequest

class UpdateDailyGoalRequestFixture {
    var deleteDailyMissionIds: List<Long> = emptyList()
    var addMissionIds: List<Long> = emptyList()

    fun withDeleteDailyMissionIds(deleteDailyMissionIds: List<Long>): UpdateDailyGoalRequestFixture =
        apply { this.deleteDailyMissionIds = deleteDailyMissionIds }

    fun withAddMissionIds(addMissionIds: List<Long>): UpdateDailyGoalRequestFixture = apply { this.addMissionIds = addMissionIds }

    fun build(): UpdateDailyGoalRequest = UpdateDailyGoalRequest(deleteDailyMissionIds, addMissionIds)
}
