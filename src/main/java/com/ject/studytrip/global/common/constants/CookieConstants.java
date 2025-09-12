package com.ject.studytrip.global.common.constants;

public final class CookieConstants {

    private CookieConstants() {}

    // 인증 관련
    public static final String AUTH_REFRESH_TOKEN = "auth_refresh";

    // OAuth 관련
    public static final String OAUTH_SIGNUP_KEY = "oauth_signup_key";
    public static final long OAUTH_SIGNUP_COOKIE_TTL_MILLIS = 900000;
}
