package com.ject.studytrip.mission.domain.policy

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.mission.domain.error.DailyMissionErrorCode
import com.ject.studytrip.mission.domain.model.DailyMission

object DailyMissionPolicy {
    fun validateNotDeleted(dailyMission: DailyMission) {
        if (dailyMission.isDeleted) {
            throw CustomException(DailyMissionErrorCode.DAILY_MISSION_ALREADY_DELETED)
        }
    }

    fun validateDailyMissionBelongsToDailyGoal(
        dailyMission: DailyMission,
        dailyGoalId: Long,
    ) {
        if (dailyMission.dailyGoal.id != dailyGoalId) {
            throw CustomException(DailyMissionErrorCode.DAILY_MISSION_NOT_BELONGS_TO_DAILY_GOAL)
        }
    }

    fun validateExistAll(
        foundDailyMissions: List<DailyMission>,
        requestedIds: List<Long>,
    ) {
        if (foundDailyMissions.size != requestedIds.size) {
            throw CustomException(DailyMissionErrorCode.DAILY_MISSION_NOT_FOUND)
        }
    }
}
