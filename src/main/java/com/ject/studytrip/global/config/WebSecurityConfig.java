package com.ject.studytrip.global.config;

import com.ject.studytrip.auth.infra.filter.JwtFilter;
import com.ject.studytrip.global.common.constants.SwaggerUrlConstants;
import com.ject.studytrip.global.common.constants.UrlConstants;
import com.ject.studytrip.global.config.properties.TokenProperties;
import com.ject.studytrip.global.security.CustomAccessDeniedHandler;
import com.ject.studytrip.global.security.CustomAuthenticationEntryPoint;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TokenProperties.class)
public class WebSecurityConfig {
    private final JwtFilter jwtFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    private void defaultFilterChain(HttpSecurity http) throws Exception {
        http
                // csrf 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                // http basic 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // 폼 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // 세션 비활성화
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // cors 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        defaultFilterChain(http);

        // JWT 필터 등록 : 인증 이전에 동작해야 하므로 UsernamePasswordAuthenticationFilter 앞에 삽입
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // 경로 인가 설정
        http.authorizeHttpRequests(
                authorize ->
                        authorize
                                .requestMatchers(SwaggerUrlConstants.getSwaggerUrls())
                                .permitAll() // Swagger 경로
                                .requestMatchers("/api/sample/**", "/api/auth/**")
                                .permitAll() // 샘플 api 경로
                                .requestMatchers("/api/trips/categories")
                                .permitAll()
                                .anyRequest()
                                .authenticated()); // 그 외 요청은 모두 인증 수행

        // 예외 핸들링
        http.exceptionHandling(
                exception ->
                        exception
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }

    // CORS 설정
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        // TODO: 환경별 CORS 허용 origin 설정 분기 처리
        //  - dev: LOCAL_DOMAIN, LOCAL_SECURE_DOMAIN, DEV_DOMAIN 허용
        //  - prod: PROD_DOMAIN 만 허용
        //  - Spring Active Profile 기반 분기 필요
        //  - 서비스 도메인, 서버 운영 환경 설정 완료 시 작업
        List<String> allowedOrigins =
                Arrays.stream(UrlConstants.values()).map(UrlConstants::getValue).toList();
        config.setAllowedOrigins(allowedOrigins);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
