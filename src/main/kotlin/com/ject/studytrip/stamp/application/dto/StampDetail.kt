package com.ject.studytrip.stamp.application.dto

import com.ject.studytrip.mission.application.dto.MissionInfo

data class StampDetail(
    val stampInfo: StampInfo,
    val missionInfos: List<MissionInfo>,
)
