package com.ject.studytrip.dummy.presentation.response.dto

import com.ject.studytrip.dummy.application.dto.DummyMissionInfo
import io.swagger.v3.oas.annotations.media.Schema

data class LoadDummyMissionInfoResponse(
    @field:Schema(description = "미션 이름")
    val missionName: String,
    @field:Schema(description = "미션 완료 여부")
    val completed: Boolean,
) {
    companion object {
        @JvmStatic
        fun of(dummyMissionInfo: DummyMissionInfo): LoadDummyMissionInfoResponse =
            LoadDummyMissionInfoResponse(
                dummyMissionInfo.missionName,
                dummyMissionInfo.completed,
            )
    }
}
