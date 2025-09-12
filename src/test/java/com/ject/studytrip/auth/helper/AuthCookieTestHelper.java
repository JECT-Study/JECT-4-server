package com.ject.studytrip.auth.helper;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieTestHelper {

    public Cookie createKakaoSignupProfileCookie(String name, String signupKey) {
        Cookie cookie = new Cookie(name, signupKey);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        return cookie;
    }

    public Cookie createRefreshTokenCookie(String name, String refreshToken) {
        Cookie cookie = new Cookie(name, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        return cookie;
    }
}
