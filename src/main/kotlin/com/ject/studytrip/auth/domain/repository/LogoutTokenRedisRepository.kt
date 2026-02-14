package com.ject.studytrip.auth.domain.repository

interface LogoutTokenRedisRepository {
    fun saveAccessToken(
        accessToken: String,
        accessTokenExpirationTime: Long,
    )

    fun existsAccessToken(accessToken: String): Boolean
}
