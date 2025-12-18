package com.ject.studytrip.mission.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class UpdateMissionRequest(
    @field:Schema(description = "수정할 미션 이름")
    @field:NotBlank(message = "새로운 미션 이름은 필수 요청 값입니다.")
    val missionName: String,
)
