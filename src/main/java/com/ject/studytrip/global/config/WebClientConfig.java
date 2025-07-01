package com.ject.studytrip.global.config;

import com.ject.studytrip.global.config.properties.KakaoOauthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(KakaoOauthProperties.class)
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }
}
