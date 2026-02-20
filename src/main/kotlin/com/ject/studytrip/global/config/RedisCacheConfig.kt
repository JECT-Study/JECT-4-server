package com.ject.studytrip.global.config

import com.ject.studytrip.global.common.constants.CacheNameConstants.DAILY_GOAL
import com.ject.studytrip.global.common.constants.CacheNameConstants.MEMBER
import com.ject.studytrip.global.common.constants.CacheNameConstants.STAMP
import com.ject.studytrip.global.common.constants.CacheNameConstants.STAMPS
import com.ject.studytrip.global.common.constants.CacheNameConstants.STUDY_LOGS
import com.ject.studytrip.global.common.constants.CacheNameConstants.TRIP
import com.ject.studytrip.global.common.constants.CacheNameConstants.TRIPS
import com.ject.studytrip.global.common.constants.CacheNameConstants.TRIP_REPORT
import com.ject.studytrip.global.common.constants.CacheNameConstants.TRIP_REPORTS
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.CacheKeyPrefix
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.cache.RedisCacheWriter.nonLockingRedisCacheWriter
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@EnableCaching
@Configuration
class RedisCacheConfig {
    @Bean
    fun redisCacheManager(factory: RedisConnectionFactory): RedisCacheManager {
        val common = createRedisCacheConfig().entryTtl(Duration.ofMinutes(30)) // 기본 TTL

        return RedisCacheManager
            .builder(nonLockingRedisCacheWriter(factory))
            .cacheDefaults(common)
            .withInitialCacheConfigurations(redisCacheConfigsByName(common))
            .transactionAware()
            .build()
    }

    // 공통 직렬화/프리픽스/널 캐싱 금지 설정
    private fun createRedisCacheConfig(): RedisCacheConfiguration =
        RedisCacheConfiguration
            .defaultCacheConfig()
            .serializeKeysWith(fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(fromSerializer(GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues()
            .computePrefixWith(CacheKeyPrefix.simple())

    // 캐시 이름별 TTL 정책 적용
    private fun redisCacheConfigsByName(common: RedisCacheConfiguration): Map<String, RedisCacheConfiguration> =
        mutableMapOf<String, RedisCacheConfiguration>().apply {
            put(MEMBER, common.entryTtl(Duration.ofMinutes(10))) // 멤버 상세 조회
            put(DAILY_GOAL, common.entryTtl(Duration.ofMinutes(10))) // 데일리 목표 상세 조회
            put(STUDY_LOGS, common.entryTtl(Duration.ofMinutes(10))) // 학습 로그 목록 조회
            put(TRIP_REPORTS, common.entryTtl(Duration.ofMinutes(10))) // 여행 리포트 목록 조회
            put(TRIP, common.entryTtl(Duration.ofMinutes(5))) // 여행 상세 조회
            put(STAMP, common.entryTtl(Duration.ofMinutes(5))) // 스탬프 상세 조회
            put(TRIP_REPORT, common.entryTtl(Duration.ofMinutes(5))) // 여행 리포트 상세 조회
            put(TRIPS, common.entryTtl(Duration.ofMinutes(3))) // 여행 목록 조회(페이지)
            put(STAMPS, common.entryTtl(Duration.ofMinutes(3))) // 스탬프 목록 조회
        }
}
