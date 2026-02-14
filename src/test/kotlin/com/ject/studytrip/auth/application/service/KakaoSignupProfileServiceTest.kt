package com.ject.studytrip.auth.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.auth.application.dto.KakaoSignupProfile
import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.domain.repository.KakaoSignupProfileRedisRepository
import com.ject.studytrip.global.exception.CustomException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import java.util.Optional

@DisplayName("KakaoSignupProfileService 단위 테스트")
class KakaoSignupProfileServiceTest : BaseUnitTest() {
    @InjectMocks
    lateinit var kakaoSignupProfileService: KakaoSignupProfileService

    @Mock
    lateinit var kakaoSignupProfileRedisRepository: KakaoSignupProfileRedisRepository

    companion object {
        private const val VALID_SIGNUP_KEY = "kakao::signup::profile:valid-key"
        private const val INVALID_SIGNUP_KEY = "kakao::signup::profile:invalid-key"
        private const val KAKAO_ID = "12345"
        private const val KAKAO_PROVIDER = "kakao"
        private const val EMAIL = "studytrip@kakao.com"
        private const val PROFILE_IMAGE = "https://kakao.com/profile.jpg"
    }

    @Nested
    @DisplayName("saveAndIssueSignupKey 메서드는")
    inner class SaveAndIssueSignupKey {
        @Test
        @DisplayName("프로필 저장 후 발급된 키를 반환한다.")
        fun shouldReturnIssuedKey() {
            // given
            val issuedKey = "issued-signup-key"
            given(kakaoSignupProfileRedisRepository.saveAndIssueSignupKey(KAKAO_ID, EMAIL, PROFILE_IMAGE)).willReturn(issuedKey)

            // when
            val result = kakaoSignupProfileService.saveAndIssueSignupKey(KAKAO_ID, EMAIL, PROFILE_IMAGE)

            // then
            assertThat(result).isEqualTo(issuedKey)
        }
    }

    @Nested
    @DisplayName("getSignupProfileByKey 메서드는")
    inner class GetSignupProfileByKey {
        @Test
        @DisplayName("signupKey가 null이면 예외가 발생한다.")
        fun shouldThrowExceptionWhenSignupKeyIsNull() {
            // when
            val exception = assertThrows<CustomException> { kakaoSignupProfileService.getSignupProfileByKey(null) }

            // then
            assertThat(exception.message).isEqualTo(AuthErrorCode.MISSING_KAKAO_SIGNUP_KEY.message)
        }

        @Test
        @DisplayName("signupKey가 빈 문자열이면 예외가 발생한다.")
        fun shouldThrowExceptionWhenSignupKeyIsBlank() {
            // when
            val exception = assertThrows<CustomException> { kakaoSignupProfileService.getSignupProfileByKey("") }

            // then
            assertThat(exception.message).isEqualTo(AuthErrorCode.MISSING_KAKAO_SIGNUP_KEY.message)
        }

        @Test
        @DisplayName("signupKey가 공백이면 예외가 발생한다.")
        fun shouldThrowExceptionWhenSignupKeyIsWhitespace() {
            // when
            val exception = assertThrows<CustomException> { kakaoSignupProfileService.getSignupProfileByKey("   ") }

            // then
            assertThat(exception.message).isEqualTo(AuthErrorCode.MISSING_KAKAO_SIGNUP_KEY.message)
        }

        @Test
        @DisplayName("signupKey가 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenSignupKeyDoesNotExist() {
            // given
            given(kakaoSignupProfileRedisRepository.findBySignupKey(INVALID_SIGNUP_KEY)).willReturn(Optional.empty())

            // when
            val exception = assertThrows<CustomException> { kakaoSignupProfileService.getSignupProfileByKey(INVALID_SIGNUP_KEY) }

            // then
            assertThat(exception.message).isEqualTo(AuthErrorCode.INVALID_KAKAO_SIGNUP_KEY.message)
        }

        @Test
        @DisplayName("유효한 signupKey가 들어오면 KakaoSignupProfile을 반환한다.")
        fun shouldReturnKakaoSignupProfileWhenKeyIsValid() {
            // given
            val kakaoProfile = KakaoSignupProfile(KAKAO_ID, KAKAO_PROVIDER, EMAIL, PROFILE_IMAGE)
            given(kakaoSignupProfileRedisRepository.findBySignupKey(VALID_SIGNUP_KEY)).willReturn(Optional.of(kakaoProfile))

            // when
            val result = kakaoSignupProfileService.getSignupProfileByKey(VALID_SIGNUP_KEY)

            // then
            assertThat(result).isEqualTo(kakaoProfile)
        }
    }

    @Nested
    @DisplayName("deleteBySignupKey 메서드는")
    inner class DeleteBySignupKey {
        @Test
        @DisplayName("특정 signupKey를 삭제한다")
        fun shouldDeleteSignupKey() {
            // when
            kakaoSignupProfileService.deleteBySignupKey(VALID_SIGNUP_KEY)

            // then
            verify(kakaoSignupProfileRedisRepository).deleteBySignupKey(VALID_SIGNUP_KEY)
        }
    }
}
