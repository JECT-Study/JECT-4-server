package com.ject.studytrip.mission.domain.repository

import com.ject.studytrip.mission.domain.model.DailyMission

interface DailyMissionRepository {
    fun save(dailyMission: DailyMission): DailyMission

    fun saveAll(dailyMissions: List<DailyMission>): List<DailyMission>

    fun findAllByIdIn(dailyMissionIds: List<Long>): List<DailyMission>
}
