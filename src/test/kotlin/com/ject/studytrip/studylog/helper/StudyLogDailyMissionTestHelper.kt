package com.ject.studytrip.studylog.helper

import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionRepository
import com.ject.studytrip.studylog.fixture.StudyLogDailyMissionFixture
import org.springframework.stereotype.Component

@Component
class StudyLogDailyMissionTestHelper(
    private val studyLogDailyMissionRepository: StudyLogDailyMissionRepository,
) {
    fun saveStudyLogDailyMissions(
        studyLog: StudyLog,
        dailyMission: DailyMission,
    ): List<StudyLogDailyMission> =
        studyLogDailyMissionRepository.saveAll(listOf(StudyLogDailyMissionFixture(studyLog, dailyMission).create()))
}
