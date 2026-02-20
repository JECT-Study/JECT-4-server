package com.ject.studytrip.mission.application.dto

import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.mission.domain.model.DailyMission

data class DailyMissionInfo(
    val dailyMissionId: Long,
    val missionInfo: MissionInfo,
) {
    companion object {
        fun from(dailyMission: DailyMission): DailyMissionInfo =
            DailyMissionInfo(dailyMission.id.requireId(), MissionInfo.from(dailyMission.mission))
    }
}
