package com.ject.studytrip.mission.helper

import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.mission.domain.repository.MissionRepository
import com.ject.studytrip.mission.fixture.MissionFixture
import com.ject.studytrip.stamp.domain.model.Stamp
import org.springframework.stereotype.Component

@Component
class MissionTestHelper(
    private val missionRepository: MissionRepository,
) {
    fun saveMission(stamp: Stamp): Mission = missionRepository.save(MissionFixture(stamp).create())

    fun saveDeletedMission(stamp: Stamp): Mission = missionRepository.save(MissionFixture(stamp).create().also { it.updateDeletedAt() })

    fun saveCompletedMission(stamp: Stamp): Mission = missionRepository.save(MissionFixture(stamp).create().also { it.updateCompleted() })
}
