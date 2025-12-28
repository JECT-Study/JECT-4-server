package com.ject.studytrip.stamp.application.dto

import com.ject.studytrip.mission.application.dto.MissionInfo

data class StampDetail(
    val stampInfo: StampInfo,
    val missionInfos: List<MissionInfo>,
) {
    companion object {
        @JvmStatic
        fun from(
            stampInfo: StampInfo,
            missionInfos: List<MissionInfo>,
        ): StampDetail = StampDetail(stampInfo, missionInfos)
    }
}
