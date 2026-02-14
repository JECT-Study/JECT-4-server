package com.ject.studytrip.auth.infra.repository.redis

import com.ject.studytrip.auth.domain.repository.LogoutTokenRedisRepository
import com.ject.studytrip.global.common.constants.CacheKeyConstants.AUTH_LOGOUT_TOKEN_PREFIX
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class LogoutTokenRedisRepositoryAdapter(
    private val redisTemplate: RedisTemplate<String, String>,
) : LogoutTokenRedisRepository {
    companion object {
        private const val LOGOUT_MARKER = "LOGOUT"
    }

    override fun saveAccessToken(
        accessToken: String,
        accessTokenExpirationTime: Long,
    ) {
        redisTemplate.opsForValue().set(buildKey(accessToken), LOGOUT_MARKER, accessTokenExpirationTime, TimeUnit.MILLISECONDS)
    }

    override fun existsAccessToken(accessToken: String): Boolean = redisTemplate.hasKey(buildKey(accessToken)) == true

    private fun buildKey(accessToken: String): String = AUTH_LOGOUT_TOKEN_PREFIX + accessToken
}
