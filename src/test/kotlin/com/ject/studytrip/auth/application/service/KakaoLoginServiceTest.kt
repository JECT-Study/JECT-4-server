package com.ject.studytrip.auth.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.fixture.KakaoTokenResponseFixture
import com.ject.studytrip.auth.fixture.KakaoUserInfoResponseFixture
import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse
import com.ject.studytrip.auth.infra.provider.KakaoOauthProvider
import com.ject.studytrip.global.exception.CustomException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`

@DisplayName("KakaoLoginService 단위 테스트")
class KakaoLoginServiceTest : BaseUnitTest() {
    @InjectMocks
    lateinit var kakaoLoginService: KakaoLoginService

    @Mock
    lateinit var kakaoOauthProvider: KakaoOauthProvider

    companion object {
        private const val KAKAO_ID = "12345"
        private const val EMAIL = "studytrip@kakao.com"
        private const val PROFILE_IMAGE = "https://kakao.com/profile.jpg"
        private const val VALID_CODE = "kakao-auth-code-1234567890"
        private const val VALID_ORIGIN = "https://test.com"
    }

    @Nested
    @DisplayName("getKakaoUserInfo 메서드는")
    inner class GetKakaoUserInfo {
        @Test
        @DisplayName("유효하지 않은 인가 코드를 전달하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenAuthorizationCodeIsInvalid() {
            // given
            `when`(
                kakaoOauthProvider.getKakaoTokens(" ", VALID_ORIGIN),
            ).thenThrow(CustomException(AuthErrorCode.INVALID_KAKAO_AUTHORIZATION_CODE))

            // when
            val exception = assertThrows<CustomException> { kakaoLoginService.getKakaoUserInfo(" ", VALID_ORIGIN) }

            // then
            assertThat(exception.message).isEqualTo(AuthErrorCode.INVALID_KAKAO_AUTHORIZATION_CODE.message)
        }

        @Test
        @DisplayName("카카오 토큰 응답은 왔지만 사용자 정보 조회에 실패하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenFetchingKakaoUserInfoFails() {
            // given
            val tokenResponse: KakaoTokenResponse = KakaoTokenResponseFixture().build()
            `when`(kakaoOauthProvider.getKakaoTokens(VALID_CODE, VALID_ORIGIN)).thenReturn(tokenResponse)
            `when`(
                kakaoOauthProvider.getKakaoUserInfo(tokenResponse.accessToken),
            ).thenThrow(CustomException(AuthErrorCode.KAKAO_USER_INFO_FETCH_FAILED))

            // when
            val exception = assertThrows<CustomException> { kakaoLoginService.getKakaoUserInfo(VALID_CODE, VALID_ORIGIN) }

            // then
            assertThat(exception.message).isEqualTo(AuthErrorCode.KAKAO_USER_INFO_FETCH_FAILED.message)
        }

        @Test
        @DisplayName("유효한 인가 코드와 origin을 전달하면 사용자 정보를 반환한다.")
        fun shouldReturnKakaoUserInfoResponseWhenCodeAndOriginAreValid() {
            // given
            val kakaoTokenResponse = KakaoTokenResponseFixture().build()
            `when`(kakaoOauthProvider.getKakaoTokens(VALID_CODE, VALID_ORIGIN)).thenReturn(kakaoTokenResponse)
            val kakaoUserInfoResponse = KakaoUserInfoResponseFixture().build()
            `when`(kakaoOauthProvider.getKakaoUserInfo(kakaoTokenResponse.accessToken)).thenReturn(kakaoUserInfoResponse)

            // when
            val result = kakaoLoginService.getKakaoUserInfo(VALID_CODE, VALID_ORIGIN)

            // then
            assertThat(result).isEqualTo(kakaoUserInfoResponse)
            assertThat(result.kakaoId).isEqualTo(KAKAO_ID)
            assertThat(result.email).isEqualTo(EMAIL)
            assertThat(result.profileImage).isEqualTo(PROFILE_IMAGE)
        }
    }
}
