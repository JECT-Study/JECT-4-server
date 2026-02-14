package com.ject.studytrip.auth.infra.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class KakaoTokenResponse(
    @field:Schema(description = "카카오 토큰 타입")
    @field:JsonProperty("token_type")
    val tokenType: String,
    @field:Schema(description = "카카오 엑세스 토큰")
    @field:JsonProperty("access_token")
    val accessToken: String,
    @field:Schema(description = "카카오 엑세스 토큰 만료 시간")
    @field:JsonProperty("expires_in")
    val accessExpiresIn: Int,
    @field:Schema(description = "카카오 리프레시 토큰")
    @field:JsonProperty("refresh_token")
    val refreshToken: String,
    @field:Schema(description = "카카오 리프레시 토큰 만료 시간")
    @field:JsonProperty("refresh_token_expires_in")
    val refreshExpiresIn: Int,
    @field:Schema(description = "카카오 스코프")
    @field:JsonProperty("scope")
    val scope: String,
)
