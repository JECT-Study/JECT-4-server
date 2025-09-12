package com.ject.studytrip.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.domain.model.KakaoSignupProfile;
import com.ject.studytrip.auth.domain.repository.KakaoSignupProfileRedisRepository;
import com.ject.studytrip.global.exception.CustomException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("KakaoSignupProfileService 단위 테스트")
class KakaoSignupProfileServiceTest extends BaseUnitTest {
    private static final String VALID_SIGNUP_KEY = "kakao::signup::profile:valid-key";
    private static final String INVALID_SIGNUP_KEY = "kakao::signup::profile:invalid-key";
    private static final String KAKAO_ID = "12345";
    private static final String KAKAO_PROVIDER = "kakao";
    private static final String EMAIL = "test@kakao.com";
    private static final String PROFILE_IMAGE = "https://kakao.com/profile.jpg";

    @InjectMocks private KakaoSignupProfileService kakaoSignupProfileService;

    @Mock private KakaoSignupProfileRedisRepository kakaoSignupProfileRedisRepository;

    @Nested
    @DisplayName("saveAndIssueSignupKey 메서드는")
    class SaveAndIssueSignupKey {

        @Test
        @DisplayName("프로필 저장 후 발급된 키를 반환한다.")
        void shouldReturnIssuedKey() {
            // given
            String issuedKey = "issued-signup-key";
            given(
                            kakaoSignupProfileRedisRepository.saveAndIssueSignupKey(
                                    KAKAO_ID, EMAIL, PROFILE_IMAGE))
                    .willReturn(issuedKey);

            // when
            String result =
                    kakaoSignupProfileService.saveAndIssueSignupKey(KAKAO_ID, EMAIL, PROFILE_IMAGE);

            // then
            assertThat(result).isEqualTo(issuedKey);
            verify(kakaoSignupProfileRedisRepository)
                    .saveAndIssueSignupKey(KAKAO_ID, EMAIL, PROFILE_IMAGE);
        }
    }

    @Nested
    @DisplayName("getSignupProfileByKey 메서드는")
    class GetSignupProfileByKey {

        @Test
        @DisplayName("유효한 signupKey로 조회하면 KakaoSignupProfile을 반환한다.")
        void shouldReturnKakaoSignupProfileWhenKeyIsValid() {
            // given
            KakaoSignupProfile expectedProfile =
                    KakaoSignupProfile.of(KAKAO_ID, KAKAO_PROVIDER, EMAIL, PROFILE_IMAGE);
            given(kakaoSignupProfileRedisRepository.findBySignupKey(VALID_SIGNUP_KEY))
                    .willReturn(Optional.of(expectedProfile));

            // when
            KakaoSignupProfile result =
                    kakaoSignupProfileService.getSignupProfileByKey(VALID_SIGNUP_KEY);

            // then
            assertThat(result).isEqualTo(expectedProfile);
        }

        @Test
        @DisplayName("signupKey가 null이면 MISSING_KAKAO_SIGNUP_KEY 예외가 발생한다.")
        void shouldThrowExceptionWhenSignupKeyIsNull() {
            // when & then
            assertThatThrownBy(() -> kakaoSignupProfileService.getSignupProfileByKey(null))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(AuthErrorCode.MISSING_KAKAO_SIGNUP_KEY.getMessage());
        }

        @Test
        @DisplayName("signupKey가 빈 문자열이면 MISSING_KAKAO_SIGNUP_KEY 예외가 발생한다.")
        void shouldThrowExceptionWhenSignupKeyIsBlank() {
            // when & then
            assertThatThrownBy(() -> kakaoSignupProfileService.getSignupProfileByKey(""))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(AuthErrorCode.MISSING_KAKAO_SIGNUP_KEY.getMessage());
        }

        @Test
        @DisplayName("signupKey가 공백만 있으면 MISSING_KAKAO_SIGNUP_KEY 예외가 발생한다.")
        void shouldThrowExceptionWhenSignupKeyIsWhitespace() {
            // when & then
            assertThatThrownBy(() -> kakaoSignupProfileService.getSignupProfileByKey("   "))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(AuthErrorCode.MISSING_KAKAO_SIGNUP_KEY.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 signupKey로 조회하면 INVALID_KAKAO_SIGNUP_KEY 예외가 발생한다.")
        void shouldThrowExceptionWhenSignupKeyDoesNotExist() {
            // given
            given(kakaoSignupProfileRedisRepository.findBySignupKey(INVALID_SIGNUP_KEY))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                            () ->
                                    kakaoSignupProfileService.getSignupProfileByKey(
                                            INVALID_SIGNUP_KEY))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(AuthErrorCode.INVALID_KAKAO_SIGNUP_KEY.getMessage());
        }
    }
}
