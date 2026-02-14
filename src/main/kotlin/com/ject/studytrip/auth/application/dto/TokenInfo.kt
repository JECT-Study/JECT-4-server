package com.ject.studytrip.auth.application.dto

data class TokenInfo(
    val accessToken: String,
    val refreshToken: String,
    val refreshTokenExpiresIn: Long,
)
