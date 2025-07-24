package com.ject.studytrip.auth.fixture;

import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse;

public class KakaoTokenResponseFixture {

    public KakaoTokenResponse build() {
        return new KakaoTokenResponse(
                "bearer", "access-token", 3600, "refresh-token", 7200, "scope");
    }
}
