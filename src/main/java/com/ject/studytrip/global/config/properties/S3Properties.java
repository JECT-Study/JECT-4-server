package com.ject.studytrip.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
        String bucket,
        String region,
        long presignExpiresInMinutes,
        S3Retry retry,
        S3Timeout timeout) {
    public record S3Retry(int maxAttempts) {}

    public record S3Timeout(int apiCallInSeconds, int apiCallAttemptInSeconds) {}
}
