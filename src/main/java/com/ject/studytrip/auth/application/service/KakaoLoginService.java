package com.ject.studytrip.auth.application.service;

import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;
import com.ject.studytrip.auth.infra.provider.KakaoOauthProvider;
import com.ject.studytrip.auth.infra.provider.TokenProvider;
import com.ject.studytrip.auth.presentation.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoLoginService {
    private final KakaoOauthProvider kakaoOauthProvider;
    private final TokenProvider tokenProvider;

    public KakaoUserInfoResponse getKakaoUserInfo(String code) {
        KakaoTokenResponse response = kakaoOauthProvider.getKakaoTokens(code);
        return kakaoOauthProvider.getKakaoUserInfo(response.accessToken());
    }

    public TokenResponse getTokens(String memberId, String memberRole) {
        String accessToken = tokenProvider.createAccessToken(memberId, memberRole);
        String refreshToken = tokenProvider.createRefreshToken(memberId, memberRole);
        return TokenResponse.of(accessToken, refreshToken);
    }
}
