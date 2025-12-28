package com.ject.studytrip.mission.presentation.dto.response

import com.ject.studytrip.mission.application.dto.MissionInfo
import io.swagger.v3.oas.annotations.media.Schema

data class LoadMissionInfoResponse(
    @field:Schema(description = "미션 ID")
    val missionId: Long,
    @field:Schema(description = "미션 이름")
    val missionName: String,
    @field:Schema(description = "미션 완료 여부")
    val completed: Boolean,
) {
    companion object {
        @JvmStatic
        fun of(missionInfo: MissionInfo): LoadMissionInfoResponse =
            LoadMissionInfoResponse(missionInfo.missionId, missionInfo.missionName, missionInfo.completed)
    }
}
