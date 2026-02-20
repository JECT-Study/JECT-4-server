package com.ject.studytrip.global.config

import com.ject.studytrip.global.config.properties.KakaoOauthProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(KakaoOauthProperties::class)
class WebClientConfig {
    @Bean
    fun webClient(): WebClient =
        WebClient
            .builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build()
}
