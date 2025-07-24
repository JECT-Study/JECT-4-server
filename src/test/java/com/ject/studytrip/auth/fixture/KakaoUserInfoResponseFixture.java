package com.ject.studytrip.auth.fixture;

import com.ject.studytrip.auth.infra.dto.KakaoAccount;
import com.ject.studytrip.auth.infra.dto.KakaoProfile;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;

public class KakaoUserInfoResponseFixture {
    private String kakaoId = "12345";
    private String email = "choi@kakao.com";
    private String profileImage = "https://kakao.com/profile.jpg";

    public KakaoUserInfoResponseFixture withKakaoId(String kakaoId) {
        this.kakaoId = kakaoId;
        return this;
    }

    public KakaoUserInfoResponseFixture withEmail(String email) {
        this.email = email;
        return this;
    }

    public KakaoUserInfoResponseFixture withProfileImage(String profileImage) {
        this.profileImage = profileImage;
        return this;
    }

    public KakaoUserInfoResponseFixture withKakaoIdAndEmail(String kakaoId, String email) {
        this.kakaoId = kakaoId;
        this.email = email;
        return this;
    }

    public KakaoUserInfoResponse build() {
        return new KakaoUserInfoResponse(
                kakaoId, new KakaoAccount(new KakaoProfile(profileImage), email));
    }
}
