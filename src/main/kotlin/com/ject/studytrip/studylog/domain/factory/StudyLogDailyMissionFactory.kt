package com.ject.studytrip.studylog.domain.factory

import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission

object StudyLogDailyMissionFactory {
    fun create(
        studyLog: StudyLog,
        dailyMission: DailyMission,
    ): StudyLogDailyMission = StudyLogDailyMission.of(studyLog, dailyMission)
}
