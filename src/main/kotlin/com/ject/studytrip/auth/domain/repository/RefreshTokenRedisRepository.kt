package com.ject.studytrip.auth.domain.repository

interface RefreshTokenRedisRepository {
    fun saveRefreshToken(
        memberId: String,
        refreshToken: String,
        refreshTokenExpireTime: Long,
    )

    fun existsRefreshToken(refreshToken: String?): Boolean

    fun deleteRefreshToken(refreshToken: String?)

    fun findMemberIdByRefreshToken(refreshToken: String?): String?
}
