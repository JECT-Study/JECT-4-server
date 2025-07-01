package com.ject.studytrip.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOauthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String tokenUri,
        String userInfoUri) {}
