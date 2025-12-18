package com.ject.studytrip.mission.helper

import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.mission.domain.repository.DailyMissionRepository
import com.ject.studytrip.mission.fixture.DailyMissionFixture
import com.ject.studytrip.trip.domain.model.DailyGoal
import org.springframework.stereotype.Component

@Component
class DailyMissionTestHelper(
    private val dailyMissionRepository: DailyMissionRepository,
) {
    fun saveDailyMission(
        mission: Mission,
        dailyGoal: DailyGoal,
    ): DailyMission = dailyMissionRepository.save(DailyMissionFixture(mission, dailyGoal).create())

    fun saveDeletedDailyMission(
        mission: Mission,
        dailyGoal: DailyGoal,
    ): DailyMission = dailyMissionRepository.save(DailyMissionFixture(mission, dailyGoal).create().also { it.updateDeletedAt() })
}
