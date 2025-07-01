package com.ject.studytrip.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record TokenProperties(
        String secret, long accessExpirationTime, long refreshExpirationTime) {}
