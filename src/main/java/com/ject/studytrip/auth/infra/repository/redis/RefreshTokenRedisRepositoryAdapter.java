package com.ject.studytrip.auth.infra.repository.redis;

import static com.ject.studytrip.global.common.constants.CacheKeyConstants.AUTH_REISSUE_TOKEN_PREFIX;

import com.ject.studytrip.auth.domain.repository.RefreshTokenRedisRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepositoryAdapter implements RefreshTokenRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void saveRefreshToken(
            String memberId, String refreshToken, long refreshTokenExpireTime) {
        redisTemplate
                .opsForValue()
                .set(
                        AUTH_REISSUE_TOKEN_PREFIX.getValue() + refreshToken,
                        memberId,
                        refreshTokenExpireTime,
                        TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean existsRefreshToken(String refreshToken) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(AUTH_REISSUE_TOKEN_PREFIX.getValue() + refreshToken));
    }

    @Override
    public void deleteRefreshToken(String refreshToken) {
        redisTemplate.delete(AUTH_REISSUE_TOKEN_PREFIX.getValue() + refreshToken);
    }

    @Override
    public String findMemberIdByRefreshToken(String refreshToken) {
        return redisTemplate.opsForValue().get(AUTH_REISSUE_TOKEN_PREFIX.getValue() + refreshToken);
    }
}
