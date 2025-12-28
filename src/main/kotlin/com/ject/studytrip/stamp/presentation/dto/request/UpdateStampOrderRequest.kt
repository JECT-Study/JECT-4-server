package com.ject.studytrip.stamp.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

data class UpdateStampOrderRequest(
    @field:Schema(description = "변경된 순서를 반영한 스탬프 ID 목록")
    @field:NotEmpty(message = "스탬프 순서를 변경하려면 최소 1개 이상의 ID가 필요합니다.")
    val orderedStampIds: List<Long>,
)
