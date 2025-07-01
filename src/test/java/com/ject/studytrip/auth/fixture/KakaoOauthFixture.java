package com.ject.studytrip.auth.fixture;

import com.ject.studytrip.auth.infra.dto.KakaoAccount;
import com.ject.studytrip.auth.infra.dto.KakaoProfile;
import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;
import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest;
import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest;

public class KakaoOauthFixture {
    private static final String KAKAO_ID = "12345";
    private static final String EMAIL = "choi@kakao.com";
    private static final String PROFILE_IMAGE = "https://kakao.com/profile.jpg";
    private static final String VALID_CODE = "valid-code";
    private static final String NICKNAME = "민우";
    private static final String CATEGORY = "STUDENT";

    public static KakaoUserInfoResponse createKakaoUserInfoResponse() {
        return new KakaoUserInfoResponse(
                KAKAO_ID, new KakaoAccount(new KakaoProfile(PROFILE_IMAGE), EMAIL));
    }

    public static KakaoLoginRequest createLoginRequest() {
        return new KakaoLoginRequest(VALID_CODE);
    }

    public static KakaoSignupRequest createSignupRequest() {
        return new KakaoSignupRequest(VALID_CODE, CATEGORY, NICKNAME);
    }

    public static KakaoTokenResponse createTokenResponse() {
        return new KakaoTokenResponse(
                "bearer", "access-token", 3600, "refresh-token", 7200, "scope");
    }
}
