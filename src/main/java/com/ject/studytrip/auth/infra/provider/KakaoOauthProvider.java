package com.ject.studytrip.auth.infra.provider;

import static org.springframework.util.StringUtils.hasText;

import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.infra.client.KakaoOauthClient;
import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;
import com.ject.studytrip.global.config.properties.KakaoOauthProperties;
import com.ject.studytrip.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

@Component
@RequiredArgsConstructor
public class KakaoOauthProvider {
    private final KakaoOauthClient kakaoOauthClient;
    private final KakaoOauthProperties kakaoOauthProperties;

    public KakaoTokenResponse getKakaoTokens(String code) {
        validateKakaoAuthorizationCode(code);
        return kakaoOauthClient
                .fetchKakaoTokens(kakaoOauthProperties.tokenUri(), createFormData(code))
                .block();
    }

    public KakaoUserInfoResponse getKakaoUserInfo(String accessToken) {
        validateKakaoToken(accessToken);
        return kakaoOauthClient
                .fetchKakaoUserInfo(kakaoOauthProperties.userInfoUri(), accessToken)
                .block();
    }

    private BodyInserters.FormInserter<String> createFormData(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoOauthProperties.clientId());
        formData.add("client_secret", kakaoOauthProperties.clientSecret());
        formData.add("redirect_uri", kakaoOauthProperties.redirectUri());
        formData.add("code", code);
        return BodyInserters.fromFormData(formData);
    }

    private void validateKakaoAuthorizationCode(String code) {
        if (!hasText(code)) {
            throw new CustomException(AuthErrorCode.INVALID_KAKAO_AUTHORIZATION_CODE);
        }
    }

    private void validateKakaoToken(String accessToken) {
        if (!hasText(accessToken)) {
            throw new CustomException(AuthErrorCode.INVALID_KAKAO_TOKEN);
        }
    }
}
