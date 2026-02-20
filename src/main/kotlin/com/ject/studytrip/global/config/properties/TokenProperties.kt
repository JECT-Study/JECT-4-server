package com.ject.studytrip.global.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class TokenProperties(
    val secret: String,
    val accessExpirationTime: Long,
    val refreshExpirationTime: Long,
)
