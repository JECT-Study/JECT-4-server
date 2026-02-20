package com.ject.studytrip.global.config

import com.ject.studytrip.global.config.properties.S3Properties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.core.retry.RetryMode
import software.amazon.awssdk.core.retry.RetryPolicy
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.time.Duration

@Configuration
@EnableConfigurationProperties(S3Properties::class)
class S3Config(
    private val s3Properties: S3Properties,
) {
    @Bean
    fun s3Client(): S3Client {
        val retryPolicy =
            RetryPolicy
                .builder(RetryMode.STANDARD)
                .numRetries(s3Properties.retry.maxAttempts)
                .build()

        return S3Client
            .builder()
            .overrideConfiguration {
                it
                    .retryPolicy(retryPolicy)
                    .apiCallTimeout(Duration.ofSeconds(s3Properties.timeout.apiCallInSeconds))
                    .apiCallAttemptTimeout(Duration.ofSeconds(s3Properties.timeout.apiCallAttemptInSeconds))
            }.region(Region.of(s3Properties.region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()
    }

    @Bean
    fun s3Presigner(): S3Presigner =
        S3Presigner
            .builder()
            .region(Region.of(s3Properties.region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()
}
