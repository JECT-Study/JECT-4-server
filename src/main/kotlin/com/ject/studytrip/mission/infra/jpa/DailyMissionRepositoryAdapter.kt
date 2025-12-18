package com.ject.studytrip.mission.infra.jpa

import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.mission.domain.repository.DailyMissionRepository
import org.springframework.stereotype.Repository

@Repository
class DailyMissionRepositoryAdapter(
    private val dailyMissionJpaRepository: DailyMissionJpaRepository,
) : DailyMissionRepository {
    override fun save(dailyMission: DailyMission): DailyMission = dailyMissionJpaRepository.save(dailyMission)

    override fun saveAll(dailyMissions: List<DailyMission>): List<DailyMission> = dailyMissionJpaRepository.saveAll(dailyMissions)

    override fun findAllByIdIn(dailyMissionIds: List<Long>): List<DailyMission> = dailyMissionJpaRepository.findAllByIdIn(dailyMissionIds)
}
