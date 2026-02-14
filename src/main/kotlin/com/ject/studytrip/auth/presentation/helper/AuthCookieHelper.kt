package com.ject.studytrip.auth.presentation.helper

import com.ject.studytrip.global.common.constants.CookieConstants.AUTH_REFRESH_TOKEN
import com.ject.studytrip.global.common.constants.CookieConstants.OAUTH_SIGNUP_COOKIE_TTL_MILLIS
import com.ject.studytrip.global.common.constants.CookieConstants.OAUTH_SIGNUP_KEY
import org.springframework.http.ResponseCookie
import java.time.Duration

object AuthCookieHelper {
    fun setOAuthSignupProfileCookie(value: String): ResponseCookie =
        setResponseCookie(OAUTH_SIGNUP_KEY, value, Duration.ofMillis(OAUTH_SIGNUP_COOKIE_TTL_MILLIS))

    fun setRefreshTokenCookie(
        value: String,
        maxAgeInSeconds: Long,
    ): ResponseCookie = setResponseCookie(AUTH_REFRESH_TOKEN, value, Duration.ofSeconds(maxAgeInSeconds))

    fun clearCookie(name: String): ResponseCookie =
        ResponseCookie
            .from(name, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(Duration.ZERO)
            .build()

    private fun setResponseCookie(
        name: String,
        value: String,
        maxAge: Duration,
    ): ResponseCookie =
        ResponseCookie
            .from(name, value)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .maxAge(maxAge)
            .path("/")
            .build()
}
