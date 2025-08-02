package com.ject.studytrip.auth.fixture;

import com.ject.studytrip.auth.presentation.dto.request.LogoutRequest;

public class LogoutRequestFixture {
    private String accessToken = null;
    private String refreshToken = null;

    public LogoutRequestFixture withAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public LogoutRequestFixture withRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public LogoutRequest build() {
        return new LogoutRequest(accessToken, refreshToken);
    }
}
