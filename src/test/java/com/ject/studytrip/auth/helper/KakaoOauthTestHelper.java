package com.ject.studytrip.auth.helper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;
import com.ject.studytrip.auth.infra.provider.KakaoOauthProvider;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KakaoOauthTestHelper {
    private final KakaoOauthProvider kakaoOauthProvider;

    @Autowired
    public KakaoOauthTestHelper(KakaoOauthProvider kakaoOauthProvider) {
        this.kakaoOauthProvider = kakaoOauthProvider;
    }

    public void mockSuccess(
            KakaoTokenResponse kakaoTokenResponse, KakaoUserInfoResponse kakaoUserInfoResponse) {
        given(kakaoOauthProvider.getKakaoTokens(anyString())).willReturn(kakaoTokenResponse);
        given(kakaoOauthProvider.getKakaoUserInfo(anyString())).willReturn(kakaoUserInfoResponse);
    }

    public void mockThrowException(
            KakaoTokenResponse kakaoTokenResponse, MemberErrorCode memberErrorCode) {
        given(kakaoOauthProvider.getKakaoTokens(anyString())).willReturn(kakaoTokenResponse);
        given(kakaoOauthProvider.getKakaoUserInfo(anyString()))
                .willThrow(new CustomException(memberErrorCode));
    }
}
