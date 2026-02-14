package com.ject.studytrip.auth.fixture

import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse

class KakaoTokenResponseFixture(
    private val tokenType: String = "bearer",
    private val accessToken: String = "access-token",
    private val accessTokenExpiresIn: Int = 3600,
    private val refreshToken: String = "refresh-token",
    private val refreshTokenExpiresIn: Int = 7200,
    private val scope: String = "scope",
) {
    fun build(): KakaoTokenResponse =
        KakaoTokenResponse(tokenType, accessToken, accessTokenExpiresIn, refreshToken, refreshTokenExpiresIn, scope)
}
