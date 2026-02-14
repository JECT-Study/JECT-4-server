package com.ject.studytrip.auth.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ReissueTokenResponse(
    @field:Schema(description = "새로 발급된 엑세스 토큰")
    val accessToken: String,
)
