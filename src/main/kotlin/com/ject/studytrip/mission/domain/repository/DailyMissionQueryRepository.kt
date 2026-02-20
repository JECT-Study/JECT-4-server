package com.ject.studytrip.mission.domain.repository

import com.ject.studytrip.mission.domain.model.DailyMission

interface DailyMissionQueryRepository {
    fun findAllByDailyGoalIdFetchJoinMission(dailyGoalId: Long): List<DailyMission>

    fun findAllWithMissionAndStampByIds(ids: List<Long>): List<DailyMission>
}
