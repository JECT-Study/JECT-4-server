package com.ject.studytrip.global.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UrlConstants {
    DEV_API_SERVER_URL("https://dev-api-studytrip.duckdns.org"),
    LOCAL_API_SERVER_URL("http://localhost:8080"),

    // TODO: 개발, 운영 도메인 URL 추가 작업
    LOCAL_DOMAIN_URL("http://localhost:5173"),
    LOCAL_SECURE_DOMAIN_URL("https://localhost:5173"),
    ;

    private final String value;
}
