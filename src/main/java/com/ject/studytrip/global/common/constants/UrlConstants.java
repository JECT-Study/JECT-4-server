package com.ject.studytrip.global.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UrlConstants {
    DEV_API_SERVER_URL("https://dev-api-studytrip.duckdns.org"),
    LOCAL_API_SERVER_URL("http://localhost:8080"),

    PRODUCTION_CLIENT_URL("https://ject-4-client.vercel.app"),
    LOCAL_CLIENT_URL("http://localhost:5173"),
    LOCAL_SECURE_CLIENT_URL("https://localhost:5173"),
    ;

    private final String value;
}
