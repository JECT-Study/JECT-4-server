package com.ject.studytrip.studylog.application.dto

import com.ject.studytrip.mission.application.dto.DailyMissionInfo
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission

data class StudyLogDailyMissionInfo(
    val studyLogDailyMissionId: Long,
    val dailyMissionInfo: DailyMissionInfo,
) {
    companion object {
        @JvmStatic
        fun from(studyLogDailyMission: StudyLogDailyMission): StudyLogDailyMissionInfo =
            StudyLogDailyMissionInfo(
                studyLogDailyMission.getId(),
                DailyMissionInfo.from(studyLogDailyMission.getDailyMission()),
            )
    }
}
