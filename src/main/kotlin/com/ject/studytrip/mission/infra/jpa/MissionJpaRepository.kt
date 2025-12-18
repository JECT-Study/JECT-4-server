package com.ject.studytrip.mission.infra.jpa

import com.ject.studytrip.mission.domain.model.Mission
import org.springframework.data.jpa.repository.JpaRepository

interface MissionJpaRepository : JpaRepository<Mission, Long> {
    fun findAllByIdIn(missionIds: List<Long>): List<Mission>

    fun findAllByStampIdAndDeletedAtIsNullOrderByCreatedAt(stampId: Long): List<Mission>
}
