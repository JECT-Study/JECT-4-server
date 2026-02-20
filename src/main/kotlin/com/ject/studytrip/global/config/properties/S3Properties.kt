package com.ject.studytrip.global.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "aws.s3")
data class S3Properties(
    val bucket: String,
    val region: String,
    val presignExpiresInMinutes: Long,
    val retry: S3Retry,
    val timeout: S3Timeout,
) {
    data class S3Retry(
        val maxAttempts: Int,
    )

    data class S3Timeout(
        val apiCallInSeconds: Long,
        val apiCallAttemptInSeconds: Long,
    )
}
