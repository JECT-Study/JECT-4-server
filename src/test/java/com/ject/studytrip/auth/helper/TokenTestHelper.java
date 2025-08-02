package com.ject.studytrip.auth.helper;

import com.ject.studytrip.auth.infra.provider.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenTestHelper {

    private final TokenProvider tokenProvider;

    @Autowired
    public TokenTestHelper(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public String createAccessToken(String memberId, String role) {
        return tokenProvider.createAccessToken(memberId, role);
    }

    public String createRefreshToken() {
        return tokenProvider.createRefreshToken();
    }
}
