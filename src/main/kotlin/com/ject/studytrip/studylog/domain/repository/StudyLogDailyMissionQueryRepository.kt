package com.ject.studytrip.studylog.domain.repository

import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission

interface StudyLogDailyMissionQueryRepository {
    fun findStudyLogDailyMissionsGroupedByStudyLogId(studyLogIds: List<Long>): Map<Long, List<StudyLogDailyMission>>
}
