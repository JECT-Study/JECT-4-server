package com.ject.studytrip.auth.helper

import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse
import com.ject.studytrip.auth.infra.provider.KakaoOauthProvider
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.member.domain.error.MemberErrorCode
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.springframework.stereotype.Component

@Component
class KakaoOauthTestHelper(
    private val kakaoOauthProvider: KakaoOauthProvider,
) {
    fun mockSuccess(
        kakaoTokenResponse: KakaoTokenResponse,
        kakaoUserInfoResponse: KakaoUserInfoResponse,
    ) {
        given(kakaoOauthProvider.getKakaoTokens(anyString(), anyString())).willReturn(kakaoTokenResponse)
        given(kakaoOauthProvider.getKakaoUserInfo(anyString())).willReturn(kakaoUserInfoResponse)
    }

    fun mockThrowException(
        kakaoTokenResponse: KakaoTokenResponse,
        memberErrorCode: MemberErrorCode,
    ) {
        given(kakaoOauthProvider.getKakaoTokens(anyString(), anyString())).willReturn(kakaoTokenResponse)
        given(kakaoOauthProvider.getKakaoUserInfo(anyString())).willThrow(CustomException(memberErrorCode))
    }
}
