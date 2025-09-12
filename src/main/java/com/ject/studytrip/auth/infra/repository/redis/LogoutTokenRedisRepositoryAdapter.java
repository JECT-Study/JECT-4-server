package com.ject.studytrip.auth.infra.repository.redis;

import static com.ject.studytrip.global.common.constants.CacheKeyConstants.AUTH_LOGOUT_TOKEN_PREFIX;

import com.ject.studytrip.auth.domain.repository.LogoutTokenRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LogoutTokenRedisRepositoryAdapter implements LogoutTokenRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void saveAccessToken(String accessToken, long accessTokenExpirationTime) {
        redisTemplate
                .opsForValue()
                .set(AUTH_LOGOUT_TOKEN_PREFIX + accessToken, "LOGOUT", accessTokenExpirationTime);
    }

    @Override
    public boolean existsAccessToken(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(AUTH_LOGOUT_TOKEN_PREFIX + accessToken));
    }
}
