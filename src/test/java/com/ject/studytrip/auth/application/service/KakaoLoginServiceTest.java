package com.ject.studytrip.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.fixture.KakaoOauthFixture;
import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;
import com.ject.studytrip.auth.infra.provider.KakaoOauthProvider;
import com.ject.studytrip.auth.infra.provider.TokenProvider;
import com.ject.studytrip.auth.presentation.dto.response.TokenResponse;
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
    private static final String MEMBER_ID = "123";
    private static final String ROLE = "ROLE_USER";

    @InjectMocks private KakaoLoginService kakaoLoginService;

    @Mock private KakaoOauthProvider kakaoOauthProvider;

    @Mock private TokenProvider tokenProvider;

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
            KakaoTokenResponse tokenResponse = KakaoOauthFixture.createTokenResponse();
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
            KakaoTokenResponse kakaoTokenResponse = KakaoOauthFixture.createTokenResponse();
            KakaoUserInfoResponse kakaoUserInfoResponse =
                    KakaoOauthFixture.createKakaoUserInfoResponse();
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

    @Nested
    @DisplayName("getTokens 메서드는")
    class GetTokens {

        @Test
        @DisplayName("memberId와 memberRole이 주어지면 토큰을 반환한다.")
        void shouldReturnTokenResponseWhenMemberIdAndRoleProvided() {
            // given
            String accessToken = "access.jwt.token";
            String refreshToken = "refresh.jwt.token";
            when(tokenProvider.createAccessToken(MEMBER_ID, ROLE)).thenReturn(accessToken);
            when(tokenProvider.createRefreshToken(MEMBER_ID, ROLE)).thenReturn(refreshToken);

            // when
            TokenResponse response = kakaoLoginService.getTokens(MEMBER_ID, ROLE);

            // then
            assertThat(response.accessToken()).isEqualTo(accessToken);
            assertThat(response.refreshToken()).isEqualTo(refreshToken);
        }
    }
}
