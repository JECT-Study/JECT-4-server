package com.ject.studytrip.global.common.constants;

public final class UrlConstants {

    private UrlConstants() {}

    // CORS 허용 도메인
    public static final String[] CORS_DOMAINS = {
        "http://localhost:8080",
        "https://dev-api-studytrip.duckdns.org",
        "http://localhost:5173",
        "https://localhost:5173",
        "https://ject-4-client.vercel.app"
    };

    // 정적 리소스 경로
    public static final String[] STATIC_RESOURCES = {
        "/favicon.ico",
        "/firebase-messaging-sw.js",
        "/.well-known/**",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    // OAuth 콜백 경로
    public static final String[] CALLBACK_PATHS = {"/auth/callback/**"};

    // Origin 추출이 필요한 경로
    public static final String[] ORIGIN_EXTRACT_PATHS = {"/api/auth/login/kakao"};

    // 인증이 필요없는 API 경로
    public static final String[] PERMIT_ALL_API_PATHS = {"/api/auth/**", "/api/trips/categories"};
}
