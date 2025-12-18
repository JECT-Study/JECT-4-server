package com.ject.studytrip.mission.domain.repository

import com.ject.studytrip.mission.domain.model.Mission
import java.util.Optional

interface MissionRepository {
    fun findAllByIdIn(missionIds: List<Long>): List<Mission>

    fun findAllByStampIdAndDeletedAtIsNullOrderByCreatedAt(stampId: Long): List<Mission>

    fun findById(missionId: Long): Optional<Mission>

    fun save(mission: Mission): Mission
}
