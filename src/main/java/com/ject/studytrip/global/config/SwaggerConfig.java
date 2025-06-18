package com.ject.studytrip.global.config;

import com.ject.studytrip.global.config.properties.SwaggerProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.*;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!prod") // 로컬, 개발환경 활성화
@EnableConfigurationProperties(SwaggerProperties.class)
@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private static final String SERVER_NAME = "StudyTrip";
    private static final String SERVER_DESCRIPTION = "StudyTrip 서버 URL 입니다.";
    private static final String API_TITLE = "StudyTrip 서버 API 문서";
    private static final String API_DESCRIPTION = "StudyTrip 서버 API 문서입니다.";
    private static final String GITHUB_URL = "https://github.com/JECT-Study/JECT-4-server";

    private final SwaggerProperties swaggerProperties;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .servers(swaggerServer())
                .addSecurityItem(securityRequirement())
                .components(authComponents())
                .info(swaggerInfo());
    }

    private List<Server> swaggerServer() {
        Server server =
                new Server().url(swaggerProperties.serverUrl()).description(SERVER_DESCRIPTION);
        return List.of(server);
    }

    private Components authComponents() {
        return new Components()
                .addSecuritySchemes(
                        "accessToken",
                        new SecurityScheme()
                                .type(Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(In.HEADER)
                                .name("Authorization"));
    }

    private SecurityRequirement securityRequirement() {
        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList("accessToken");
        return securityRequirement;
    }

    private Info swaggerInfo() {
        License license = new License();
        license.setUrl(GITHUB_URL);
        license.setName(SERVER_NAME);

        return new Info()
                .version("v" + swaggerProperties.version())
                .title(API_TITLE)
                .description(API_DESCRIPTION)
                .license(license);
    }
}
