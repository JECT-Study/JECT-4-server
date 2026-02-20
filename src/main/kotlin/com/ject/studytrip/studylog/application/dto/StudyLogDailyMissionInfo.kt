package com.ject.studytrip.studylog.application.dto

import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.mission.application.dto.DailyMissionInfo
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission

data class StudyLogDailyMissionInfo(
    val studyLogDailyMissionId: Long,
    val dailyMissionInfo: DailyMissionInfo,
) {
    companion object {
        fun from(studyLogDailyMission: StudyLogDailyMission): StudyLogDailyMissionInfo =
            StudyLogDailyMissionInfo(studyLogDailyMission.id.requireId(), DailyMissionInfo.from(studyLogDailyMission.dailyMission))
    }
}
