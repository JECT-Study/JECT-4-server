package com.ject.studytrip.mission.application.service

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.mission.domain.error.MissionErrorCode
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.mission.domain.policy.MissionPolicy
import com.ject.studytrip.mission.domain.repository.MissionQueryRepository
import com.ject.studytrip.mission.domain.repository.MissionRepository
import org.springframework.stereotype.Service

@Service
class MissionQueryService(
    private val missionRepository: MissionRepository,
    private val missionQueryRepository: MissionQueryRepository,
) {
    fun getValidMission(
        stampId: Long,
        missionId: Long,
    ): Mission {
        val mission =
            missionRepository
                .findById(missionId)
                .orElseThrow { CustomException(MissionErrorCode.MISSION_NOT_FOUND) }

        MissionPolicy.validateMissionBelongsToStamp(stampId, mission)
        MissionPolicy.validateNotDeleted(mission)
        MissionPolicy.validateNotCompleted(mission)

        return mission
    }

    fun getMissionsByStampId(stampId: Long): List<Mission> = missionRepository.findAllByStampIdAndDeletedAtIsNullOrderByCreatedAt(stampId)

    fun getValidMissionsByIds(missionIds: List<Long>): List<Mission> {
        val missions = missionQueryRepository.findAllByIdsInFetchJoinStamp(missionIds)

        MissionPolicy.validateExistAll(missions, missionIds)
        missions.forEach { mission ->
            MissionPolicy.validateNotDeleted(mission)
            MissionPolicy.validateNotCompleted(mission)
        }

        return missions
    }
}
