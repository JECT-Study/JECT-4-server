package com.ject.studytrip.trip.presentation.dto.request

import com.ject.studytrip.pomodoro.presentation.dto.request.CreatePomodoroRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CreateDailyGoalRequest(
    @field:Schema(name = "뽀모도로")
    @field:Valid
    val pomodoro: CreatePomodoroRequest,
    @field:Schema(name = "미션 ID 목록")
    @field:NotEmpty(message = "미션 목록은 필수 요청 값입니다.")
    val missionIds: List<
        @NotNull(message = "미션 ID는 필수 요청 값입니다.")
        Long,
    >,
)
