package com.ject.studytrip.global.config

import com.ject.studytrip.auth.application.service.TokenService
import com.ject.studytrip.global.common.constants.UrlConstants
import com.ject.studytrip.global.config.properties.TokenProperties
import com.ject.studytrip.global.security.CustomAuthenticationEntryPoint
import com.ject.studytrip.global.security.filter.JwtAuthenticationFilter
import com.ject.studytrip.global.security.filter.OriginExtractionFilter
import com.ject.studytrip.global.security.handler.CustomAccessDeniedHandler
import com.ject.studytrip.global.security.handler.SecurityResponseHandler
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@EnableWebSecurity
@Configuration
@EnableConfigurationProperties(TokenProperties::class)
class WebSecurityConfig(
    private val authenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val accessDeniedHandler: CustomAccessDeniedHandler,
) {
    // 퍼블릭 리소스 URL 필터 체인
    @Bean
    @Order(0)
    fun publicFilterChain(http: HttpSecurity): SecurityFilterChain {
        defaultFilterChain(http)

        http.securityMatcher(*UrlConstants.STATIC_RESOURCES).authorizeHttpRequests { it.anyRequest().permitAll() }

        return http.build()
    }

    // 콜백 URL 필터 체인
    @Bean
    @Order(1)
    fun callbackFilterChain(http: HttpSecurity): SecurityFilterChain {
        defaultFilterChain(http)

        http.securityMatcher(*UrlConstants.CALLBACK_PATHS).authorizeHttpRequests { it.anyRequest().permitAll() }

        return http.build()
    }

    // Origin 추출이 필요한 URL 필터 체인
    @Bean
    @Order(2)
    fun originExtractionFilterChain(
        http: HttpSecurity,
        securityResponseHandler: SecurityResponseHandler,
    ): SecurityFilterChain {
        defaultFilterChain(http)

        http.securityMatcher(*UrlConstants.ORIGIN_EXTRACT_PATHS)

        // Origin 추출 필터 등록: CORS 이후 OriginExtractionFilter 실행
        http.addFilterAfter(OriginExtractionFilter(securityResponseHandler), CorsFilter::class.java)

        http.authorizeHttpRequests { it.anyRequest().permitAll() }

        return http.build()
    }

    // 메인 필터 체인
    @Bean
    @Order(3)
    fun mainFilterChain(
        http: HttpSecurity,
        tokenService: TokenService,
    ): SecurityFilterChain {
        defaultFilterChain(http)

        // JWT 필터 등록: 인증 이전에 동작해야 하므로 UsernamePasswordAuthenticationFilter 앞에 삽입
        http.addFilterBefore(JwtAuthenticationFilter(tokenService), UsernamePasswordAuthenticationFilter::class.java)

        // 경로 인가 설정
        http.authorizeHttpRequests {
            it
                .requestMatchers(*UrlConstants.PERMIT_ALL_API_PATHS)
                .permitAll()
                .anyRequest()
                .authenticated()
        }

        // 예외 핸들링
        http.exceptionHandling {
            it
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
        }

        return http.build()
    }

    // CORS 설정
    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val config =
            CorsConfiguration().apply {
                allowCredentials = true
                addAllowedHeader("*")
                addAllowedMethod("*")
                allowedOrigins = UrlConstants.CORS_DOMAINS.toList()
            }

        return UrlBasedCorsConfigurationSource().apply { registerCorsConfiguration("/**", config) }
    }

    private fun defaultFilterChain(http: HttpSecurity) {
        http
            .csrf { it.disable() } // csrf 비활성화
            .httpBasic { it.disable() } // http basic 비활성화
            .formLogin { it.disable() } // 폼 로그인 비활성화
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // 세션 비활성화
            .cors { it.configurationSource(corsConfigurationSource()) } // cors 설정
    }
}
