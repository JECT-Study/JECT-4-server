package com.ject.studytrip.global.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cdn")
data class CdnProperties(
    val domain: String,
)
