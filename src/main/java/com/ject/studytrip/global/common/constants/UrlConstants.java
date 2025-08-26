package com.ject.studytrip.global.common.constants;

import lombok.Getter;

@Getter
public enum UrlConstants {
    // CORS 허용 도메인
    CORS_DOMAINS(
            "http://localhost:8080",
            "https://dev-api-studytrip.duckdns.org",
            "http://localhost:5173",
            "https://localhost:5173",
            "https://ject-4-client.vercel.app"),

    // 정적 리소스 경로
    STATIC_RESOURCES(
            "/favicon.ico",
            "/firebase-messaging-sw.js",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"),

    // OAuth 콜백 경로
    CALLBACK_PATHS("/auth/callback/**"),

    // Origin 추출이 필요한 경로
    ORIGIN_EXTRACT_PATHS("/api/auth/login/kakao", "/api/auth/signup/kakao"),

    // 인증이 필요없는 API 경로
    PERMIT_ALL_API_PATHS("/api/auth/**", "/api/trips/categories");

    private final String[] urls;

    UrlConstants(String... urls) {
        this.urls = urls;
    }
}
