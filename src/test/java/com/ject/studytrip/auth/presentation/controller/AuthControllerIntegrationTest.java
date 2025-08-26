package com.ject.studytrip.auth.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ject.studytrip.BaseIntegrationTest;
import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.domain.repository.RefreshTokenRedisRepository;
import com.ject.studytrip.auth.fixture.*;
import com.ject.studytrip.auth.fixture.TokenReissueRequestFixture;
import com.ject.studytrip.auth.helper.KakaoOauthTestHelper;
import com.ject.studytrip.auth.helper.TokenTestHelper;
import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;
import com.ject.studytrip.auth.infra.provider.KakaoOauthProvider;
import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest;
import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest;
import com.ject.studytrip.auth.presentation.dto.request.LogoutRequest;
import com.ject.studytrip.auth.presentation.dto.request.TokenReissueRequest;
import com.ject.studytrip.global.exception.error.CommonErrorCode;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.helper.MemberTestHelper;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("AuthController 통합 테스트")
class AuthControllerIntegrationTest extends BaseIntegrationTest {
    private static final String BASE_AUTH_URL = "/api/auth";
    private static final String TEST_ORIGIN = "http://localhost:8080";

    @Autowired private MemberTestHelper memberTestHelper;
    @Autowired private TokenTestHelper tokenTestHelper;
    @Autowired private KakaoOauthTestHelper kakaoOauthTestHelper;
    @Autowired private RefreshTokenRedisRepository refreshTokenRedisRepository;

    @MockitoBean KakaoOauthProvider kakaoOauthProvider;

    private Member member;
    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        member = memberTestHelper.saveMember();
        accessToken =
                tokenTestHelper.createAccessToken(
                        member.getId().toString(), member.getRole().name());
        refreshToken = tokenTestHelper.createRefreshToken();

        long refreshTokenExpirationTime = Duration.ofSeconds(30).getSeconds();
        refreshTokenRedisRepository.saveRefreshToken(
                member.getId().toString(), refreshToken, refreshTokenExpirationTime);
    }

    @Nested
    @DisplayName("카카오 로그인 API")
    class KakaoLogin {
        private final KakaoTokenResponseFixture kakaoTokenResponseFixture =
                new KakaoTokenResponseFixture();
        private final KakaoLoginRequestFixture kakaoLoginRequestFixture =
                new KakaoLoginRequestFixture();
        private final KakaoUserInfoResponseFixture kakaoUserInfoResponseFixture =
                new KakaoUserInfoResponseFixture();

        private ResultActions getResultActions(KakaoLoginRequest request) throws Exception {
            return mockMvc.perform(
                    post(BASE_AUTH_URL + "/login/kakao")
                            .header("Origin", TEST_ORIGIN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("가입되지 않은 사용자 인가 코드로 로그인 시 409 Conflict를 반환한다.")
        void shouldReturnConflictWhenMemberNotSignUp() throws Exception {
            // given
            member.updateDeletedAt();
            KakaoLoginRequest request = kakaoLoginRequestFixture.build();
            KakaoTokenResponse kakaoTokenResponse = kakaoTokenResponseFixture.build();
            kakaoOauthTestHelper.mockThrowException(
                    kakaoTokenResponse, MemberErrorCode.MEMBER_NEED_SIGNUP);

            // when
            ResultActions resultActions = getResultActions(request);

            // then
            resultActions
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(MemberErrorCode.MEMBER_NEED_SIGNUP.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.error")
                                    .value(MemberErrorCode.MEMBER_NEED_SIGNUP.name()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(MemberErrorCode.MEMBER_NEED_SIGNUP.getMessage()));
        }

        @Test
        @DisplayName("탈퇴한 사용자가 인가 코드로 로그인 시 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenMemberAlreadyDeleted() throws Exception {
            // given
            member.updateDeletedAt();
            KakaoLoginRequest request = kakaoLoginRequestFixture.build();
            KakaoTokenResponse kakaoTokenResponse = kakaoTokenResponseFixture.build();
            kakaoOauthTestHelper.mockThrowException(
                    kakaoTokenResponse, MemberErrorCode.MEMBER_ALREADY_DELETED);

            // when
            ResultActions resultActions = getResultActions(request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            MemberErrorCode.MEMBER_ALREADY_DELETED
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(MemberErrorCode.MEMBER_ALREADY_DELETED.getMessage()));
        }

        @Test
        @DisplayName("가입된 사용자의 인가 코드로 로그인하면 토큰이 발급된다.")
        void shouldReturnTokenResponseWhenLoginIsSuccessful() throws Exception {
            // given
            KakaoLoginRequest request = kakaoLoginRequestFixture.build();
            KakaoTokenResponse kakaoTokenResponse = kakaoTokenResponseFixture.build();
            KakaoUserInfoResponse kakaoUserInfoResponse = kakaoUserInfoResponseFixture.build();
            kakaoOauthTestHelper.mockSuccess(kakaoTokenResponse, kakaoUserInfoResponse);

            // when
            ResultActions resultActions = getResultActions(request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("카카오 회원가입 API")
    class KakaoSignup {
        private final KakaoTokenResponseFixture kakaoTokenResponseFixture =
                new KakaoTokenResponseFixture();
        private final KakaoSignupRequestFixture kakaoSignupRequestFixture =
                new KakaoSignupRequestFixture();
        private final KakaoUserInfoResponseFixture kakaoUserInfoResponseFixture =
                new KakaoUserInfoResponseFixture();

        private ResultActions getResultActions(KakaoSignupRequest request) throws Exception {
            return mockMvc.perform(
                    post(BASE_AUTH_URL + "/signup/kakao")
                            .header("Origin", TEST_ORIGIN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("이미 가입된 사용자가 회원가입 요청 시 409 Conflict를 반환한다.")
        void shouldThrowExceptionWhenSignupForExistingMember() throws Exception {
            // given
            KakaoSignupRequest request = kakaoSignupRequestFixture.build();
            KakaoTokenResponse kakaoTokenResponse = kakaoTokenResponseFixture.build();
            KakaoUserInfoResponse kakaoUserInfoResponse = kakaoUserInfoResponseFixture.build();
            kakaoOauthTestHelper.mockSuccess(kakaoTokenResponse, kakaoUserInfoResponse);

            // when
            ResultActions resultActions = getResultActions(request);

            // then
            resultActions
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            MemberErrorCode.MEMBER_ALREADY_EXISTS
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(MemberErrorCode.MEMBER_ALREADY_EXISTS.getMessage()));
        }

        @Test
        @DisplayName("회원가입 요청 시 유효한 정보라면 토큰이 발급된다.")
        void shouldReturnTokenResponseWhenSignupIsSuccessful() throws Exception {
            // given
            memberTestHelper.deleteMemberById(member.getId());
            KakaoSignupRequest request = kakaoSignupRequestFixture.build();
            KakaoTokenResponse kakaoTokenResponse = kakaoTokenResponseFixture.build();
            KakaoUserInfoResponse kakaoUserInfoResponse = kakaoUserInfoResponseFixture.build();
            kakaoOauthTestHelper.mockSuccess(kakaoTokenResponse, kakaoUserInfoResponse);

            // when
            ResultActions resultActions = getResultActions(request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("토큰 재발급 API")
    class ReissueToken {
        private final TokenReissueRequestFixture tokenReissueRequestFixture =
                new TokenReissueRequestFixture();

        private ResultActions getResultActions(TokenReissueRequest request) throws Exception {
            return mockMvc.perform(
                    post(BASE_AUTH_URL + "/token/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("리프레시 토큰이 null 이면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenRefreshTokenIsNull() throws Exception {
            // given
            TokenReissueRequest request = tokenReissueRequestFixture.build();

            // when
            ResultActions resultActions = getResultActions(request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            CommonErrorCode.METHOD_ARGUMENT_NOT_VALID
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.getMessage()));
        }

        @Test
        @DisplayName("리프레시 토큰이 존재하지 않거나 위조된 경우 401 UNAUTHORIZED를 반환한다.")
        void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {
            // given
            TokenReissueRequest request =
                    tokenReissueRequestFixture.withRefreshToken("invalid.refresh.token").build();

            // when
            ResultActions resultActions = getResultActions(request);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.INVALID_REFRESH_TOKEN.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.INVALID_REFRESH_TOKEN.getMessage()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 새로운 엑세스 토큰과 리프레시 토큰을 재발급한다.")
        void shouldReissueTokenWhenRequestIsValid() throws Exception {
            // given
            TokenReissueRequest request =
                    tokenReissueRequestFixture.withRefreshToken(refreshToken).build();

            // when
            ResultActions resultActions = getResultActions(request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("로그아웃 API")
    class Logout {
        private final LogoutRequestFixture logoutRequestFixture = new LogoutRequestFixture();

        private ResultActions getResultActions(LogoutRequest request) throws Exception {
            return mockMvc.perform(
                    post(BASE_AUTH_URL + "/logout")
                            .header("Origin", TEST_ORIGIN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("엑세스 토큰이 null 이면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenAccessTokenIsNull() throws Exception {
            // given
            LogoutRequest request = logoutRequestFixture.withRefreshToken(refreshToken).build();

            // when
            ResultActions resultActions = getResultActions(request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            CommonErrorCode.METHOD_ARGUMENT_NOT_VALID
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.getMessage()));
        }

        @Test
        @DisplayName("리프레시 토큰이 null 이면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenRefreshTokenIsNull() throws Exception {
            // given
            LogoutRequest request = logoutRequestFixture.withAccessToken(accessToken).build();

            // when
            ResultActions resultActions = getResultActions(request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            CommonErrorCode.METHOD_ARGUMENT_NOT_VALID
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.getMessage()));
        }

        @Test
        @DisplayName("엑세스 토큰이 존재하지 않거나 위조된 경우 401 UNAUTHORIZED를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsInvalid() throws Exception {
            // given
            LogoutRequest request =
                    logoutRequestFixture
                            .withAccessToken("invalid.access.token")
                            .withRefreshToken(refreshToken)
                            .build();

            // when
            ResultActions resultActions = getResultActions(request);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.INVALID_JWT_TOKEN.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.INVALID_JWT_TOKEN.getMessage()));
        }

        @Test
        @DisplayName("리프레시 토큰이 존재하지 않거나 위조된 경우 401 UNAUTHORIZED를 반환한다.")
        void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {
            // given
            LogoutRequest request =
                    logoutRequestFixture
                            .withAccessToken(accessToken)
                            .withRefreshToken("invalid.refresh.token")
                            .build();

            // when
            ResultActions resultActions = getResultActions(request);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.INVALID_REFRESH_TOKEN.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.INVALID_REFRESH_TOKEN.getMessage()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면, 엑세스 토큰을 블랙리스트에 추가하고, 저장된 리프레시 토큰을 제거합니다.")
        void shouldLogoutWhenRequestIsValid() throws Exception {
            // given
            LogoutRequest request =
                    logoutRequestFixture
                            .withAccessToken(accessToken)
                            .withRefreshToken(refreshToken)
                            .build();

            // when
            ResultActions resultActions = getResultActions(request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }
    }
}
