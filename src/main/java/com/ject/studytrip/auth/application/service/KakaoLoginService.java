package com.ject.studytrip.auth.application.service;

import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;
import com.ject.studytrip.auth.infra.provider.KakaoOauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoLoginService {
    private final KakaoOauthProvider kakaoOauthProvider;

    public KakaoUserInfoResponse getKakaoUserInfo(String code, String origin) {
        KakaoTokenResponse response = kakaoOauthProvider.getKakaoTokens(code, origin);
        return kakaoOauthProvider.getKakaoUserInfo(response.accessToken());
    }
}
