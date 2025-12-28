package com.ject.studytrip.trip.presentation.dto.response

import com.ject.studytrip.trip.application.dto.DailyGoalInfo
import io.swagger.v3.oas.annotations.media.Schema

data class CreateDailyGoalResponse(
    @field:Schema(description = "데일리 목표 ID")
    val dailyGoalId: Long,
) {
    companion object {
        @JvmStatic
        fun of(dailyGoalInfo: DailyGoalInfo): CreateDailyGoalResponse = CreateDailyGoalResponse(dailyGoalInfo.dailyGoalId)
    }
}
