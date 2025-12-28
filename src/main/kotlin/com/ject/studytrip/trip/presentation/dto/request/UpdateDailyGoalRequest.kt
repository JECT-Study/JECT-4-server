package com.ject.studytrip.trip.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class UpdateDailyGoalRequest(
    @field:Schema(name = "삭제할 데일리 미션 ID 목록")
    val deleteDailyMissionIds: List<Long>,
    @field:Schema(name = "추가할 미션 ID 목록")
    val addMissionIds: List<Long>,
)
