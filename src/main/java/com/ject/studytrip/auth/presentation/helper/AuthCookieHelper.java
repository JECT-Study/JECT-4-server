package com.ject.studytrip.auth.presentation.helper;

import static com.ject.studytrip.global.common.constants.CookieConstants.*;

import java.time.Duration;
import org.springframework.http.ResponseCookie;

public final class AuthCookieHelper {
    public static ResponseCookie setOAuthSignupProfileCookie(String value) {
        return setResponseCookie(
                OAUTH_SIGNUP_KEY, value, Duration.ofMillis(OAUTH_SIGNUP_COOKIE_TTL_MILLIS));
    }

    public static ResponseCookie setRefreshTokenCookie(String value, long maxAgeInSeconds) {
        return setResponseCookie(AUTH_REFRESH_TOKEN, value, Duration.ofSeconds(maxAgeInSeconds));
    }

    private static ResponseCookie setResponseCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(maxAge)
                .path("/")
                .build();
    }

    public static ResponseCookie clearCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
    }
}
