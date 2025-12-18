package com.ject.studytrip.mission.domain.factory

import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.trip.domain.model.DailyGoal

object DailyMissionFactory {
    @JvmStatic
    fun create(
        mission: Mission,
        dailyGoal: DailyGoal,
    ): DailyMission = DailyMission.of(mission, dailyGoal)
}
