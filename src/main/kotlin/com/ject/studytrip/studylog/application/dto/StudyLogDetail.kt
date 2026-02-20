package com.ject.studytrip.studylog.application.dto

import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission

data class StudyLogDetail(
    val studyLogInfo: StudyLogInfo,
    val studyLogDailyMissionInfos: List<StudyLogDailyMissionInfo>,
) {
    companion object {
        fun from(
            studyLog: StudyLog,
            studyLogDailyMissions: List<StudyLogDailyMission>?,
        ): StudyLogDetail {
            val studyLogDailyMissions = studyLogDailyMissions ?: emptyList()

            return StudyLogDetail(
                StudyLogInfo.from(studyLog),
                studyLogDailyMissions.map { StudyLogDailyMissionInfo.from(it) },
            )
        }
    }
}
