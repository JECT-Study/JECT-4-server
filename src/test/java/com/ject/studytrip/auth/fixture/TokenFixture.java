package com.ject.studytrip.auth.fixture;

import com.ject.studytrip.global.config.properties.TokenProperties;

public class TokenFixture {
    public static final String TEST_SECRET =
            "this-is-a-test-secret-key-which-is-long-enough-1234567890";
    public static final long ACCESS_EXPIRATION_TIME = 7200;
    public static final long REFRESH_EXPIRATION_TIME = 604800;
    public static final String TOKEN_PREFIX = "Bearer ";

    public static TokenProperties createTokenProperties() {
        return new TokenProperties(TEST_SECRET, ACCESS_EXPIRATION_TIME, REFRESH_EXPIRATION_TIME);
    }

    public static String authorization(String token) {
        return TOKEN_PREFIX + token;
    }
}
