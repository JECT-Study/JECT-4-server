package com.ject.studytrip.global.config

import com.ject.studytrip.global.config.properties.SwaggerProperties
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!prod") // 로컬, 개발환경 활성화
@Configuration
@EnableConfigurationProperties(SwaggerProperties::class)
class SwaggerConfig(
    private val swaggerProperties: SwaggerProperties,
) {
    companion object {
        private const val SERVER_NAME = "StudyTrip"
        private const val SERVER_DESCRIPTION = "StudyTrip 서버 URL 입니다."
        private const val API_TITLE = "StudyTrip 서버 API 문서"
        private const val API_DESCRIPTION = "StudyTrip 서버 API 문서입니다."
        private const val GITHUB_URL = "https://github.com/JECT-Study/JECT-4-server"
    }

    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI()
            .servers(swaggerServer())
            .addSecurityItem(securityRequirement())
            .components(authComponents())
            .info(swaggerInfo())

    private fun swaggerServer(): List<Server> {
        val server =
            Server()
                .url(swaggerProperties.serverUrl)
                .description(SERVER_DESCRIPTION)

        return listOf(server)
    }

    private fun authComponents(): Components =
        Components()
            .addSecuritySchemes(
                "accessToken",
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .`in`(SecurityScheme.In.HEADER)
                    .name("Authorization"),
            )

    private fun securityRequirement(): SecurityRequirement = SecurityRequirement().addList("accessToken")

    private fun swaggerInfo(): Info {
        val license =
            License()
                .url(GITHUB_URL)
                .name(SERVER_NAME)

        return Info()
            .version("v${swaggerProperties.version}")
            .title(API_TITLE)
            .description(API_DESCRIPTION)
            .license(license)
    }
}
