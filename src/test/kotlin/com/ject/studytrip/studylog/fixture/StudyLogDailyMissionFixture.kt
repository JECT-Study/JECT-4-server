package com.ject.studytrip.studylog.fixture

import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.studylog.domain.factory.StudyLogDailyMissionFactory
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission
import org.springframework.test.util.ReflectionTestUtils

class StudyLogDailyMissionFixture(
    private val studyLog: StudyLog,
    private val dailyMission: DailyMission,
) {
    fun create(): StudyLogDailyMission = StudyLogDailyMissionFactory.create(studyLog, dailyMission)

    fun createWithId(id: Long): StudyLogDailyMission =
        create().also {
            ReflectionTestUtils.setField(it, "id", id)
        }
}
