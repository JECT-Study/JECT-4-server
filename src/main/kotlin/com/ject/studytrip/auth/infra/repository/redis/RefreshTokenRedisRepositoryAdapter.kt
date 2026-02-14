package com.ject.studytrip.auth.infra.repository.redis

import com.ject.studytrip.auth.domain.repository.RefreshTokenRedisRepository
import com.ject.studytrip.global.common.constants.CacheKeyConstants.AUTH_REISSUE_TOKEN_PREFIX
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class RefreshTokenRedisRepositoryAdapter(
    private val redisTemplate: RedisTemplate<String, String>,
) : RefreshTokenRedisRepository {
    override fun saveRefreshToken(
        memberId: String,
        refreshToken: String,
        refreshTokenExpireTime: Long,
    ) {
        redisTemplate.opsForValue().set(buildKey(refreshToken), memberId, refreshTokenExpireTime, TimeUnit.MILLISECONDS)
    }

    override fun existsRefreshToken(refreshToken: String?): Boolean = redisTemplate.hasKey(buildKey(refreshToken)) == true

    override fun deleteRefreshToken(refreshToken: String?) {
        redisTemplate.delete(buildKey(refreshToken))
    }

    override fun findMemberIdByRefreshToken(refreshToken: String?): String? = redisTemplate.opsForValue().get(buildKey(refreshToken))

    private fun buildKey(refreshToken: String?): String = AUTH_REISSUE_TOKEN_PREFIX + refreshToken
}
