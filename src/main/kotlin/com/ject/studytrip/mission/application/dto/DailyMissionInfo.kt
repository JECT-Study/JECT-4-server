package com.ject.studytrip.mission.application.dto

import com.ject.studytrip.mission.domain.model.DailyMission

data class DailyMissionInfo(
    val dailyMissionId: Long,
    val missionInfo: MissionInfo,
) {
    companion object {
        @JvmStatic
        fun from(dailyMission: DailyMission): DailyMissionInfo =
            DailyMissionInfo(
                dailyMission.getId(),
                MissionInfo.from(dailyMission.getMission()),
            )
    }
}
