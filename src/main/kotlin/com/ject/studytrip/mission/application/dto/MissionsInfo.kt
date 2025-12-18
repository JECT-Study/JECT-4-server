package com.ject.studytrip.mission.application.dto

data class MissionsInfo(
    val missionInfos: List<MissionInfo>,
) {
    companion object {
        @JvmStatic
        fun of(missionInfos: List<MissionInfo>): MissionsInfo = MissionsInfo(missionInfos)
    }
}
