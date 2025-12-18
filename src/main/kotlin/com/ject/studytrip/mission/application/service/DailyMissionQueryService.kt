package com.ject.studytrip.mission.application.service

import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.mission.domain.policy.DailyMissionPolicy
import com.ject.studytrip.mission.domain.repository.DailyMissionQueryRepository
import com.ject.studytrip.mission.domain.repository.DailyMissionRepository
import org.springframework.stereotype.Service

@Service
class DailyMissionQueryService(
    private val dailyMissionRepository: DailyMissionRepository,
    private val dailyMissionQueryRepository: DailyMissionQueryRepository,
) {
    fun getValidDailyMissionsByIds(
        dailyGoalId: Long,
        dailyMissionIds: List<Long>,
    ): List<DailyMission> {
        val dailyMissions = dailyMissionRepository.findAllByIdIn(dailyMissionIds)
        validateDailyMissions(dailyMissions, dailyMissionIds, dailyGoalId)

        return dailyMissions
    }

    fun getValidDailyMissionsWithMissionAndStampByIds(
        dailyGoalId: Long,
        dailyMissionIds: List<Long>,
    ): List<DailyMission> {
        val dailyMissions = dailyMissionQueryRepository.findAllWithMissionAndStampByIds(dailyMissionIds)
        validateDailyMissions(dailyMissions, dailyMissionIds, dailyGoalId)

        return dailyMissions
    }

    fun getDailyMissionsByDailyGoalId(dailyGoalId: Long): List<DailyMission> =
        dailyMissionQueryRepository.findAllByDailyGoalIdFetchJoinMission(dailyGoalId)

    private fun validateDailyMissions(
        dailyMissions: List<DailyMission>,
        dailyMissionIds: List<Long>,
        dailyGoalId: Long,
    ) {
        DailyMissionPolicy.validateExistAll(dailyMissions, dailyMissionIds)
        dailyMissions.forEach { dailyMission ->
            DailyMissionPolicy.validateBelongsToDailyGoal(dailyMission, dailyGoalId)
            DailyMissionPolicy.validateNotDeleted(dailyMission)
        }
    }
}
