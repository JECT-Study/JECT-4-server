package com.ject.studytrip.global.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "swagger")
data class SwaggerProperties(
    val version: String,
    val serverUrl: String,
)
