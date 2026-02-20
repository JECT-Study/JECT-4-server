package com.ject.studytrip.stamp.presentation.dto.response

import com.ject.studytrip.mission.application.dto.MissionInfo
import com.ject.studytrip.mission.presentation.dto.response.LoadMissionInfoResponse
import com.ject.studytrip.stamp.application.dto.StampInfo
import io.swagger.v3.oas.annotations.media.Schema

data class LoadStampDetailResponse(
    @field:Schema(description = "스탬프 ID")
    val stampId: Long,
    @field:Schema(description = "스탬프 이름")
    val stampName: String,
    @field:Schema(description = "스탬프 순서")
    val stampOrder: Int,
    @field:Schema(description = "스탬프 종료일")
    val endDate: String?,
    @field:Schema(description = "스탬프에 속한 총 미션 수")
    val totalMissions: Int,
    @field:Schema(description = "스탬프에 속한 완료된 미션 수")
    val completedMissions: Int,
    @field:Schema(description = "스탬프 완료 여부")
    val completed: Boolean,
    @field:Schema(description = "미션 목록")
    val missions: List<LoadMissionInfoResponse>,
) {
    companion object {
        fun of(
            stampInfo: StampInfo,
            missionInfos: List<MissionInfo>,
        ): LoadStampDetailResponse =
            LoadStampDetailResponse(
                stampInfo.stampId,
                stampInfo.stampName,
                stampInfo.stampOrder,
                stampInfo.endDate,
                stampInfo.totalMissions,
                stampInfo.completedMissions,
                stampInfo.completed,
                missionInfos.map(LoadMissionInfoResponse::of),
            )
    }
}
