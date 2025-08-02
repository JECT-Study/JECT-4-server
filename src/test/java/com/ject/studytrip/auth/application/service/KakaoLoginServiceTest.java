package com.ject.studytrip.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.fixture.KakaoTokenResponseFixture;
import com.ject.studytrip.auth.fixture.KakaoUserInfoResponseFixture;
import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;
import com.ject.studytrip.auth.infra.provider.KakaoOauthProvider;
import com.ject.studytrip.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("KakaoLoginService 단위 테스트")
class KakaoLoginServiceTest extends BaseUnitTest {
    private static final String KAKAO_ID = "12345";
    private static final String EMAIL = "choi@kakao.com";
    private static final String PROFILE_IMAGE = "https://kakao.com/profile.jpg";
    private static final String VALID_CODE = "valid-code";

    @InjectMocks private KakaoLoginService kakaoLoginService;

    @Mock private KakaoOauthProvider kakaoOauthProvider;

    @Nested
    @DisplayName("getKakaoUserInfo 메서드는")
    class GetKakaoUserInfo {

        @Test
        @DisplayName("유효하지 않은 인가 코드를 전달하면 예외가 발생한다.")
        void shouldThrowExceptionWhenAuthorizationCodeIsInvalid() {
            // given
            when(kakaoOauthProvider.getKakaoTokens(" "))
                    .thenThrow(new CustomException(AuthErrorCode.INVALID_KAKAO_AUTHORIZATION_CODE));

            // when & then
            assertThatThrownBy(() -> kakaoLoginService.getKakaoUserInfo(" "))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(AuthErrorCode.INVALID_KAKAO_AUTHORIZATION_CODE.getMessage());
        }

        @Test
        @DisplayName("카카오 토큰 응답은 왔지만 사용자 정보 조회에 실패하면 예외가 발생한다.")
        void shouldThrowExceptionWhenFetchingKakaoUserInfoFails() {
            // given
            KakaoTokenResponse tokenResponse = new KakaoTokenResponseFixture().build();
            when(kakaoOauthProvider.getKakaoTokens(VALID_CODE)).thenReturn(tokenResponse);
            when(kakaoOauthProvider.getKakaoUserInfo(tokenResponse.accessToken()))
                    .thenThrow(new CustomException(AuthErrorCode.KAKAO_USER_INFO_FETCH_FAILED));

            // when & then
            assertThatThrownBy(() -> kakaoLoginService.getKakaoUserInfo(VALID_CODE))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(AuthErrorCode.KAKAO_USER_INFO_FETCH_FAILED.getMessage());
        }

        @Test
        @DisplayName("유효한 인가 코드를 전달하면 사용자 정보를 반환한다.")
        void shouldReturnKakaoUserInfoResponseWhenCodeIsValid() {
            // given
            KakaoTokenResponse kakaoTokenResponse = new KakaoTokenResponseFixture().build();
            KakaoUserInfoResponse kakaoUserInfoResponse =
                    new KakaoUserInfoResponseFixture().build();
            when(kakaoOauthProvider.getKakaoTokens(VALID_CODE)).thenReturn(kakaoTokenResponse);
            when(kakaoOauthProvider.getKakaoUserInfo("access-token"))
                    .thenReturn(kakaoUserInfoResponse);

            // when
            KakaoUserInfoResponse result = kakaoLoginService.getKakaoUserInfo(VALID_CODE);

            // then
            assertThat(result.kakaoId()).isEqualTo(KAKAO_ID);
            assertThat(result.getEmail()).isEqualTo(EMAIL);
            assertThat(result.getProfileImage()).isEqualTo(PROFILE_IMAGE);
        }
    }
}
