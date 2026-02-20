package com.ject.studytrip.global.common.constants

object UrlConstants {
    // CORS 허용 도메인
    val CORS_DOMAINS =
        arrayOf(
            "http://localhost:8080",
            "https://dev-api-studytrip.duckdns.org",
            "http://localhost:5173",
            "https://localhost:5173",
            "https://ject-4-client.vercel.app",
        )

    // 정적 리소스 경로
    val STATIC_RESOURCES =
        arrayOf(
            "/favicon.ico",
            "/firebase-messaging-sw.js",
            "/.well-known/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
        )

    // OAuth 콜백 경로
    val CALLBACK_PATHS =
        arrayOf(
            "/auth/callback/**",
        )

    // Origin 추출이 필요한 경로
    val ORIGIN_EXTRACT_PATHS =
        arrayOf(
            "/api/auth/login/kakao",
        )

    // 인증이 필요없는 API 경로
    val PERMIT_ALL_API_PATHS =
        arrayOf(
            "/api/auth/**",
            "/api/trips/categories",
            "/api/members/me/restore/**",
        )
}
