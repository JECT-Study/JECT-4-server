package com.ject.studytrip.dummy.presentation.dto.response

import com.ject.studytrip.dummy.application.dto.DummyStampInfo
import io.swagger.v3.oas.annotations.media.Schema

data class LoadDummyStampInfoResponse(
    @field:Schema(description = "스탬프 이름")
    val stampName: String,
    @field:Schema(description = "스탬프 순서")
    val stampOrder: Int,
    @field:Schema(description = "스탬프 종료일")
    val endDate: String?,
    @field:Schema(description = "스탬프에 속한 총 미션 개수")
    val totalMissions: Int,
    @field:Schema(description = "스탬프에 속한 완료된 미션 개수")
    val completedMissions: Int,
    @field:Schema(description = "스탬프 완료 여부")
    val completed: Boolean,
) {
    companion object {
        fun of(dummyStampInfo: DummyStampInfo): LoadDummyStampInfoResponse =
            LoadDummyStampInfoResponse(
                dummyStampInfo.stampName,
                dummyStampInfo.stampOrder,
                dummyStampInfo.endDate,
                dummyStampInfo.totalMissions,
                dummyStampInfo.completedMissions,
                dummyStampInfo.completed,
            )
    }
}
