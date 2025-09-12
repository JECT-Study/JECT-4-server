package com.ject.studytrip.auth.infra.repository.redis;

import static com.ject.studytrip.global.common.constants.CacheKeyConstants.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ject.studytrip.auth.domain.model.KakaoSignupProfile;
import com.ject.studytrip.auth.domain.repository.KakaoSignupProfileRedisRepository;
import com.ject.studytrip.member.domain.model.SocialProvider;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KakaoSignupProfileRedisRepositoryAdapter implements KakaoSignupProfileRedisRepository {
    private static final long KAKAO_SIGNUP_PROFILE_TTL_MILLIS = 900000;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String saveAndIssueSignupKey(String socialId, String email, String profileImageUrl) {
        String socialProvider = SocialProvider.KAKAO.name().toLowerCase();
        String key = issueKey(socialProvider);
        KakaoSignupProfile signupProfile =
                KakaoSignupProfile.of(socialId, socialProvider, email, profileImageUrl);

        redisTemplate
                .opsForValue()
                .set(key, signupProfile, KAKAO_SIGNUP_PROFILE_TTL_MILLIS, TimeUnit.MILLISECONDS);

        return key;
    }

    @Override
    public Optional<KakaoSignupProfile> findBySignupKey(String signupKey) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(signupKey))
                .map(value -> objectMapper.convertValue(value, KakaoSignupProfile.class));
    }

    @Override
    public void deleteBySignupKey(String signupKey) {
        redisTemplate.delete(signupKey);
    }

    private String issueKey(String socialProvider) {
        return OAUTH_SIGNUP_PROFILE_PREFIX.formatted(socialProvider) + UUID.randomUUID();
    }
}
