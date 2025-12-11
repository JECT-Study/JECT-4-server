package com.ject.studytrip.studylog.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty

data class CreateStudyLogRequest(
    @field:Schema(description = "뽀모도로 총 집중시간(초)")
    @field:Min(value = 0, message = "총 집중시간(초)은 음수일 수 없습니다.")
    val totalFocusTimeInSeconds: Int,
    @field:Schema(description = "선택한 데일리 미션 ID 목록")
    @field:NotEmpty(message = "학습로그를 작성할 데일리 미션 목록은 필수 요청 값입니다.")
    val selectedDailyMissionIds: List<Long>,
    @field:Schema(description = "학습 내용")
    val content: String,
)
