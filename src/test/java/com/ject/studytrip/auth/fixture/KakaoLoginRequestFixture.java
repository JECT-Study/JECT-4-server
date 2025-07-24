package com.ject.studytrip.auth.fixture;

import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest;

public class KakaoLoginRequestFixture {
    private String code = "valid-code";

    public KakaoLoginRequestFixture withCode(String code) {
        this.code = code;
        return this;
    }

    public KakaoLoginRequest build() {
        return new KakaoLoginRequest(code);
    }
}
