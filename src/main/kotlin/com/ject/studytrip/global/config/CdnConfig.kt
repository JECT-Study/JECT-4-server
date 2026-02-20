package com.ject.studytrip.global.config

import com.ject.studytrip.global.config.properties.CdnProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CdnProperties::class)
class CdnConfig
