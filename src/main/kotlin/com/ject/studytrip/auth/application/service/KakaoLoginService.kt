package com.ject.studytrip.auth.application.service

import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse
import com.ject.studytrip.auth.infra.provider.KakaoOauthProvider
import org.springframework.stereotype.Service

@Service
class KakaoLoginService(
    private val kakaoOauthProvider: KakaoOauthProvider,
) {
    fun getKakaoUserInfo(
        code: String,
        origin: String,
    ): KakaoUserInfoResponse {
        val response = kakaoOauthProvider.getKakaoTokens(code, origin)

        return kakaoOauthProvider.getKakaoUserInfo(response.accessToken)
    }
}
