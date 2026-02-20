package com.ject.studytrip.stamp.presentation.dto.response

import com.ject.studytrip.stamp.application.dto.StampInfo
import io.swagger.v3.oas.annotations.media.Schema

data class CreateStampResponse(
    @field:Schema(description = "스탬프 ID")
    val stampId: Long,
) {
    companion object {
        fun of(stampInfo: StampInfo): CreateStampResponse = CreateStampResponse(stampInfo.stampId)
    }
}
