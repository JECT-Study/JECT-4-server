package com.ject.studytrip.mission.domain.policy

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.mission.domain.error.MissionErrorCode
import com.ject.studytrip.mission.domain.model.Mission

object MissionPolicy {
    fun validateNotDeleted(mission: Mission) {
        if (mission.isDeleted()) {
            throw CustomException(MissionErrorCode.MISSION_ALREADY_DELETED)
        }
    }

    fun validateNotCompleted(mission: Mission) {
        if (mission.isCompleted()) {
            throw CustomException(MissionErrorCode.MISSION_ALREADY_COMPLETED)
        }
    }

    fun validateNotAllCompleted(exists: Boolean) {
        if (exists) {
            throw CustomException(MissionErrorCode.ALL_MISSIONS_NOT_COMPLETED)
        }
    }

    fun validateMissionBelongsToStamp(
        stampId: Long,
        mission: Mission,
    ) {
        if (mission.stamp.id != stampId) {
            throw CustomException(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP)
        }
    }

    fun validateExistAll(
        foundMissions: List<Mission>,
        requestedIds: List<Long>,
    ) {
        if (foundMissions.size != requestedIds.size) {
            throw CustomException(MissionErrorCode.MISSION_NOT_FOUND)
        }
    }
}
