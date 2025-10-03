package com.ject.studytrip.global.config;

import static com.ject.studytrip.global.common.constants.CacheNameConstants.*;
import static org.springframework.data.redis.cache.RedisCacheWriter.nonLockingRedisCacheWriter;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration common =
                createRedisCacheConfig().entryTtl(Duration.ofMinutes(30)); // 기본 TTL

        return RedisCacheManager.builder(nonLockingRedisCacheWriter(factory))
                .cacheDefaults(common)
                .withInitialCacheConfigurations(redisCacheConfigsByName(common))
                .transactionAware()
                .build();
    }

    // 공통 직렬화/프리픽스/널 캐싱 금지 설정
    private RedisCacheConfiguration createRedisCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues()
                .computePrefixWith(CacheKeyPrefix.simple());
    }

    // 캐시 이름별 TTL 정책 적용
    private Map<String, RedisCacheConfiguration> redisCacheConfigsByName(
            RedisCacheConfiguration common) {
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();

        configs.put(MEMBER, common.entryTtl(Duration.ofMinutes(10))); // 멤버 상세 조회
        configs.put(DAILY_GOAL, common.entryTtl(Duration.ofMinutes(10))); // 데일리 목표 상세 조회
        configs.put(STUDY_LOGS, common.entryTtl(Duration.ofMinutes(10))); // 학습 로그 목록 조회

        configs.put(TRIP, common.entryTtl(Duration.ofMinutes(5))); // 여행 상세 조회
        configs.put(STAMP, common.entryTtl(Duration.ofMinutes(5))); // 스탬프 상세 조회

        configs.put(TRIPS, common.entryTtl(Duration.ofMinutes(3))); // 여행 목록 조회(페이지)
        configs.put(STAMPS, common.entryTtl(Duration.ofMinutes(3))); // 스탬프 목록 조회

        return configs;
    }
}
