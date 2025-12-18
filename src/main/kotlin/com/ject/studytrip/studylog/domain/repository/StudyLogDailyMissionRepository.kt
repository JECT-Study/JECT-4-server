package com.ject.studytrip.studylog.domain.repository

import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission

interface StudyLogDailyMissionRepository {
    fun saveAll(studyLogDailyMissions: List<StudyLogDailyMission>): List<StudyLogDailyMission>
}
