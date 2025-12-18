package com.ject.studytrip.mission.fixture

import com.ject.studytrip.mission.domain.factory.DailyMissionFactory
import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.trip.domain.model.DailyGoal
import org.springframework.test.util.ReflectionTestUtils

class DailyMissionFixture(
    private val mission: Mission,
    private val dailyGoal: DailyGoal,
) {
    fun create(): DailyMission = DailyMissionFactory.create(mission, dailyGoal)

    fun createWithId(id: Long): DailyMission =
        create().also {
            ReflectionTestUtils.setField(it, "id", id)
        }
}
