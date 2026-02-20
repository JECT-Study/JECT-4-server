package com.ject.studytrip.mission.presentation.dto.response

import com.ject.studytrip.mission.application.dto.MissionInfo
import io.swagger.v3.oas.annotations.media.Schema

data class CreateMissionResponse(
    @field:Schema(description = "미션 ID")
    val missionId: Long,
) {
    companion object {
        fun of(missionInfo: MissionInfo): CreateMissionResponse = CreateMissionResponse(missionInfo.missionId)
    }
}
