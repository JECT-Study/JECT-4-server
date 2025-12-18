package com.ject.studytrip.mission.infra.jpa

import com.ject.studytrip.mission.domain.model.DailyMission
import org.springframework.data.jpa.repository.JpaRepository

interface DailyMissionJpaRepository : JpaRepository<DailyMission, Long> {
    fun findAllByIdIn(dailyMissionIds: List<Long>): List<DailyMission>
}
