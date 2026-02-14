package com.ject.studytrip.auth.infra.provider

import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.infra.client.KakaoOauthClient
import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse
import com.ject.studytrip.global.config.properties.KakaoOauthProperties
import com.ject.studytrip.global.exception.CustomException
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.StringUtils.hasText
import org.springframework.web.reactive.function.BodyInserters

@Component
class KakaoOauthProvider(
    private val kakaoOauthClient: KakaoOauthClient,
    private val kakaoOauthProperties: KakaoOauthProperties,
) {
    fun getKakaoTokens(
        code: String,
        origin: String,
    ): KakaoTokenResponse {
        validateKakaoAuthorizationCode(code)

        return kakaoOauthClient
            .fetchKakaoTokens(kakaoOauthProperties.tokenUri, createFormData(code, origin))
            .block()
            ?: throw CustomException(AuthErrorCode.INVALID_KAKAO_AUTHORIZATION_CODE)
    }

    fun getKakaoUserInfo(accessToken: String): KakaoUserInfoResponse {
        validateKakaoToken(accessToken)

        return kakaoOauthClient
            .fetchKakaoUserInfo(kakaoOauthProperties.userInfoUri, accessToken)
            .block()
            ?: throw CustomException(AuthErrorCode.INVALID_KAKAO_TOKEN)
    }

    private fun createFormData(
        code: String,
        origin: String,
    ) = BodyInserters.fromFormData(
        LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", kakaoOauthProperties.clientId)
            add("client_secret", kakaoOauthProperties.clientSecret)
            add("redirect_uri", origin + kakaoOauthProperties.redirectUri)
            add("code", code)
        },
    )

    private fun validateKakaoAuthorizationCode(code: String) {
        if (!hasText(code)) {
            throw CustomException(AuthErrorCode.INVALID_KAKAO_AUTHORIZATION_CODE)
        }
    }

    private fun validateKakaoToken(accessToken: String) {
        if (!hasText(accessToken)) {
            throw CustomException(AuthErrorCode.INVALID_KAKAO_TOKEN)
        }
    }
}
