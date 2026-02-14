package com.ject.studytrip.auth.helper

import jakarta.servlet.http.Cookie
import org.springframework.stereotype.Component

@Component
class AuthCookieTestHelper {
    fun createKakaoSignupProfileCookie(
        name: String,
        signupKey: String,
    ): Cookie =
        Cookie(name, signupKey).apply {
            isHttpOnly = true
            secure = true
            path = "/"
        }

    fun createRefreshTokenCookie(
        name: String,
        refreshToken: String,
    ): Cookie =
        Cookie(name, refreshToken).apply {
            isHttpOnly = true
            secure = true
            path = "/"
        }
}
