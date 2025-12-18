package com.ject.studytrip.mission.application.service

import com.ject.studytrip.mission.domain.factory.MissionFactory
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.mission.domain.policy.MissionPolicy
import com.ject.studytrip.mission.domain.repository.MissionCommandRepository
import com.ject.studytrip.mission.domain.repository.MissionRepository
import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest
import com.ject.studytrip.stamp.domain.model.Stamp
import org.springframework.stereotype.Service

@Service
class MissionCommandService(
    private val missionRepository: MissionRepository,
    private val missionCommandRepository: MissionCommandRepository,
) {
    fun createMission(
        stamp: Stamp,
        request: CreateMissionRequest,
    ): Mission {
        val mission = MissionFactory.create(stamp, request.missionName)

        return missionRepository.save(mission)
    }

    fun updateMissionNameIfPresent(
        mission: Mission,
        request: UpdateMissionRequest,
    ) = mission.updateName(request.missionName)

    fun deleteMission(mission: Mission) = mission.updateDeletedAt()

    fun completeMission(mission: Mission) {
        MissionPolicy.validateNotDeleted(mission)
        MissionPolicy.validateNotCompleted(mission)

        mission.updateCompleted()
    }

    fun validateMissionsBelongsToStamp(
        stampId: Long,
        missions: List<Mission>,
    ) = missions.forEach {
        MissionPolicy.validateMissionBelongsToStamp(stampId, it)
    }

    fun validateAllMissionsCompletedByStampId(stampId: Long) {
        val exists = missionCommandRepository.existsByStampIdAndCompletedIsFalseAndDeletedAtIsNull(stampId)
        MissionPolicy.validateAllCompleted(exists)
    }

    fun hardDeleteMissions(): Long = missionCommandRepository.deleteAllByDeletedAtIsNotNull()

    fun hardDeleteMissionsOwnedByDeletedStamp(): Long = missionCommandRepository.deleteAllByDeletedStampOwner()

    fun hardDeleteMissionsOwnedByMember(memberId: Long): Long = missionCommandRepository.deleteAllByMemberId(memberId)
}
