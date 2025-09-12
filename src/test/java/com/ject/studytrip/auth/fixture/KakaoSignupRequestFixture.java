package com.ject.studytrip.auth.fixture;

import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest;

public class KakaoSignupRequestFixture {
    private String category = "STUDENT";
    private String nickname = "민우";

    public KakaoSignupRequestFixture withCategory(String category) {
        this.category = category;
        return this;
    }

    public KakaoSignupRequestFixture withNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public KakaoSignupRequest build() {
        return new KakaoSignupRequest(category, nickname);
    }
}
