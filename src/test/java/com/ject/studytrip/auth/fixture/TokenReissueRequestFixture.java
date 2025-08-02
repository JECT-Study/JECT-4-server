package com.ject.studytrip.auth.fixture;

import com.ject.studytrip.auth.presentation.dto.request.TokenReissueRequest;

public class TokenReissueRequestFixture {
    private String refreshToken = null;

    public TokenReissueRequestFixture withRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public TokenReissueRequest build() {
        return new TokenReissueRequest(refreshToken);
    }
}
