package com.ject.studytrip.auth.infra.repository.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.ject.studytrip.auth.application.dto.KakaoSignupProfile
import com.ject.studytrip.auth.domain.repository.KakaoSignupProfileRedisRepository
import com.ject.studytrip.global.common.constants.CacheKeyConstants.OAUTH_SIGNUP_PROFILE_PREFIX
import com.ject.studytrip.member.domain.model.SocialProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID
import java.util.concurrent.TimeUnit

@Repository
class KakaoSignupProfileRedisRepositoryAdapter(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
) : KakaoSignupProfileRedisRepository {
    companion object {
        private const val KAKAO_SIGNUP_PROFILE_TTL_MILLIS = 900000L
    }

    override fun saveAndIssueSignupKey(
        socialId: String,
        email: String,
        profileImageUrl: String,
    ): String {
        val socialProvider = SocialProvider.KAKAO.name.lowercase()
        val key = issueKey(socialProvider)
        val signupProfile = KakaoSignupProfile(socialId, socialProvider, email, profileImageUrl)

        redisTemplate.opsForValue().set(key, signupProfile, KAKAO_SIGNUP_PROFILE_TTL_MILLIS, TimeUnit.MILLISECONDS)

        return key
    }

    override fun findBySignupKey(signupKey: String): Optional<KakaoSignupProfile> {
        val value = redisTemplate.opsForValue().get(signupKey) ?: return Optional.empty()
        val converted = objectMapper.convertValue(value, KakaoSignupProfile::class.java)

        return Optional.of(converted)
    }

    override fun deleteBySignupKey(signupKey: String) {
        redisTemplate.delete(signupKey)
    }

    private fun issueKey(socialProvider: String): String = OAUTH_SIGNUP_PROFILE_PREFIX.format(socialProvider) + UUID.randomUUID()
}
