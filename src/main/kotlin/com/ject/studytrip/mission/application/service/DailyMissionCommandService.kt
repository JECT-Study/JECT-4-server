package com.ject.studytrip.mission.application.service

import com.ject.studytrip.mission.domain.factory.DailyMissionFactory
import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.mission.domain.repository.DailyMissionCommandRepository
import com.ject.studytrip.mission.domain.repository.DailyMissionRepository
import com.ject.studytrip.trip.domain.model.DailyGoal
import org.springframework.stereotype.Service

@Service
class DailyMissionCommandService(
    private val dailyMissionRepository: DailyMissionRepository,
    private val dailyMissionCommandRepository: DailyMissionCommandRepository,
) {
    fun createDailyMissions(
        dailyGoal: DailyGoal,
        missions: List<Mission>,
    ): List<DailyMission> = dailyMissionRepository.saveAll(missions.map { DailyMissionFactory.create(it, dailyGoal) })

    fun deleteDailyMission(dailyMission: DailyMission) = dailyMission.updateDeletedAt()

    fun hardDeleteDailyMissions(): Long = dailyMissionCommandRepository.deleteAllByDeletedAtIsNotNull()

    fun hardDeleteDailyMissionsOwnedByDeletedMission(): Long = dailyMissionCommandRepository.deleteAllByDeletedMissionOwner()

    fun hardDeleteDailyMissionsOwnedByDeletedDailyGoal(): Long = dailyMissionCommandRepository.deleteAllByDeletedDailyGoalOwner()

    fun hardDeleteDailyMissionsOwnedByMember(memberId: Long): Long = dailyMissionCommandRepository.deleteAllByMemberId(memberId)
}
