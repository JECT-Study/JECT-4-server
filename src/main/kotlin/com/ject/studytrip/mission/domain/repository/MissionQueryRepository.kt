package com.ject.studytrip.mission.domain.repository

import com.ject.studytrip.mission.domain.model.Mission

interface MissionQueryRepository {
    fun findAllByIdsInFetchJoinStamp(ids: List<Long>): List<Mission>
}
