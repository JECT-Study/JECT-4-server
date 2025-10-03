package com.ject.studytrip.auth.presentation.controller;

import static com.ject.studytrip.global.common.constants.CookieConstants.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ject.studytrip.BaseIntegrationTest;
import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.domain.repository.KakaoSignupProfileRedisRepository;
import com.ject.studytrip.auth.domain.repository.RefreshTokenRedisRepository;
import com.ject.studytrip.auth.fixture.*;
import com.ject.studytrip.auth.helper.AuthCookieTestHelper;
import com.ject.studytrip.auth.helper.KakaoOauthTestHelper;
import com.ject.studytrip.auth.helper.TokenTestHelper;
import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;
import com.ject.studytrip.auth.infra.provider.KakaoOauthProvider;
import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest;
import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest;
import com.ject.studytrip.auth.presentation.dto.request.LogoutRequest;
import com.ject.studytrip.global.exception.error.CommonErrorCode;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.helper.MemberTestHelper;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("AuthController 통합 테스트")
class AuthControllerIntegrationTest extends BaseIntegrationTest {
    private static final String BASE_AUTH_URL = "/api/auth";
    private static final String TEST_ORIGIN = "http://localhost:8080";
    private static final String RESPONSE_COOKIE_NAME = "Set-Cookie";

    @Autowired private MemberTestHelper memberTestHelper;
    @Autowired private TokenTestHelper tokenTestHelper;
    @Autowired private KakaoOauthTestHelper kakaoOauthTestHelper;
    @Autowired private AuthCookieTestHelper authCookieTestHelper;

    @Autowired private RefreshTokenRedisRepository refreshTokenRedisRepository;
    @Autowired private KakaoSignupProfileRedisRepository kakaoSignupProfileRedisRepository;

    @MockitoBean KakaoOauthProvider kakaoOauthProvider;

    private Member member;
    private String accessToken;
    private String refreshToken;
    private String signupKey;

    @BeforeEach
    void setUp() {
        member = memberTestHelper.saveMember();
        accessToken =
                tokenTestHelper.createAccessToken(
                        member.getId().toString(), member.getRole().name());
        refreshToken = tokenTestHelper.createRefreshToken();

        signupKey =
                kakaoSignupProfileRedisRepository.saveAndIssueSignupKey(
                        member.getSocialId(), member.getEmail(), member.getProfileImage());

        long refreshTokenExpirationTime = Duration.ofSeconds(30).toMillis();
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
        @DisplayName("가입되지 않은 사용자 인가 코드로 로그인 시 회원가입 필요 응답을 반환한다.")
        void shouldReturnSignupRequiredWhenMemberNotSignUp() throws Exception {
            // given
            memberTestHelper.deleteMemberById(member.getId());
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
                    .andExpect(jsonPath("$.data.signupRequired").value(true))
                    .andExpect(jsonPath("$.data.accessToken").doesNotExist())
                    .andExpect(header().exists(RESPONSE_COOKIE_NAME))
                    .andExpect(
                            header().string(
                                            HttpHeaders.SET_COOKIE,
                                            containsString(OAUTH_SIGNUP_KEY + "=")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                    .andExpect(
                            header().string(
                                            HttpHeaders.SET_COOKIE,
                                            containsString("SameSite=None")));
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
                    .andExpect(jsonPath("$.data.signupRequired").value(false))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(header().exists(RESPONSE_COOKIE_NAME))
                    .andExpect(
                            header().string(
                                            HttpHeaders.SET_COOKIE,
                                            containsString(AUTH_REFRESH_TOKEN + "=")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                    .andExpect(
                            header().string(
                                            HttpHeaders.SET_COOKIE,
                                            containsString("SameSite=None")));
        }
    }

    @Nested
    @DisplayName("카카오 회원가입 API")
    class KakaoSignup {
        private final KakaoSignupRequestFixture kakaoSignupRequestFixture =
                new KakaoSignupRequestFixture();

        private ResultActions getResultActions(KakaoSignupRequest request, Cookie cookie)
                throws Exception {
            return mockMvc.perform(
                    post(BASE_AUTH_URL + "/signup/kakao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .cookie(cookie));
        }

        @Test
        @DisplayName("회원가입 요청 시 signupKey 쿠키가 없으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenSignupKeyCookieMissing() throws Exception {
            // given
            memberTestHelper.deleteMemberById(member.getId());
            KakaoSignupRequest request = kakaoSignupRequestFixture.build();
            Cookie nullCookie =
                    authCookieTestHelper.createKakaoSignupProfileCookie("NULL_COOKIE", signupKey);

            // when
            ResultActions resultActions = getResultActions(request, nullCookie);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            AuthErrorCode.MISSING_KAKAO_SIGNUP_KEY
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.MISSING_KAKAO_SIGNUP_KEY.getMessage()));
        }

        @Test
        @DisplayName("회원가입 요청 시 signupKey가 유효하지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenSignupKeyInvalid() throws Exception {
            // given
            memberTestHelper.deleteMemberById(member.getId());
            KakaoSignupRequest request = kakaoSignupRequestFixture.build();
            Cookie invalidCookie =
                    authCookieTestHelper.createKakaoSignupProfileCookie(
                            OAUTH_SIGNUP_KEY, "invalid.pending.key");

            // when - 유효하지 않은 쿠키로 요청
            ResultActions resultActions = getResultActions(request, invalidCookie);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            AuthErrorCode.INVALID_KAKAO_SIGNUP_KEY
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.INVALID_KAKAO_SIGNUP_KEY.getMessage()));
        }

        @Test
        @DisplayName("이미 가입된 사용자가 회원가입 요청 시 409 Conflict를 반환한다.")
        void shouldThrowExceptionWhenSignupForExistingMember() throws Exception {
            // given
            KakaoSignupRequest request = kakaoSignupRequestFixture.build();
            Cookie cookie =
                    authCookieTestHelper.createKakaoSignupProfileCookie(
                            OAUTH_SIGNUP_KEY, signupKey);

            // when
            ResultActions resultActions = getResultActions(request, cookie);

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
            Cookie cookie =
                    authCookieTestHelper.createKakaoSignupProfileCookie(
                            OAUTH_SIGNUP_KEY, signupKey);

            // when
            ResultActions resultActions = getResultActions(request, cookie);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.data.signupRequired").value(false))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(header().exists(RESPONSE_COOKIE_NAME))
                    .andExpect(
                            header().string(
                                            HttpHeaders.SET_COOKIE,
                                            containsString(AUTH_REFRESH_TOKEN + "=")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                    .andExpect(
                            header().string(
                                            HttpHeaders.SET_COOKIE,
                                            containsString("SameSite=None")));
        }
    }

    @Nested
    @DisplayName("토큰 재발급 API")
    class ReissueToken {

        private ResultActions getResultActions(Cookie cookie) throws Exception {
            return mockMvc.perform(post(BASE_AUTH_URL + "/token/reissue").cookie(cookie));
        }

        @Test
        @DisplayName("리프레시 토큰이 null 혹은 비어있을 경우 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenRefreshTokenIsNullOrBlank() throws Exception {
            // given
            Cookie nullCookie =
                    authCookieTestHelper.createRefreshTokenCookie(
                            "null.refresh.cookie", refreshToken);

            // when
            ResultActions resultActions = getResultActions(nullCookie);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.MISSING_REFRESH_TOKEN.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.MISSING_REFRESH_TOKEN.getMessage()));
        }

        @Test
        @DisplayName("리프레시 토큰이 존재하지 않거나 위조된 경우 401 UNAUTHORIZED를 반환한다.")
        void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {
            // given
            Cookie invalidRefreshCookie =
                    authCookieTestHelper.createRefreshTokenCookie(
                            AUTH_REFRESH_TOKEN, "invalid:refresh:cookie");

            // when
            ResultActions resultActions = getResultActions(invalidRefreshCookie);

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
            Cookie refreshCookie =
                    authCookieTestHelper.createRefreshTokenCookie(AUTH_REFRESH_TOKEN, refreshToken);

            // when
            ResultActions resultActions = getResultActions(refreshCookie);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(header().exists(RESPONSE_COOKIE_NAME))
                    .andExpect(
                            header().string(
                                            HttpHeaders.SET_COOKIE,
                                            containsString(AUTH_REFRESH_TOKEN + "=")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                    .andExpect(
                            header().string(
                                            HttpHeaders.SET_COOKIE,
                                            containsString("SameSite=None")));
        }
    }

    @Nested
    @DisplayName("로그아웃 API")
    class Logout {
        private final LogoutRequestFixture logoutRequestFixture = new LogoutRequestFixture();

        private ResultActions getResultActions(LogoutRequest request, Cookie cookie)
                throws Exception {
            return mockMvc.perform(
                    post(BASE_AUTH_URL + "/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .cookie(cookie));
        }

        @Test
        @DisplayName("엑세스 토큰이 null 이면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenAccessTokenIsNull() throws Exception {
            // given
            LogoutRequest request = logoutRequestFixture.build();
            Cookie refreshCookie =
                    authCookieTestHelper.createRefreshTokenCookie(AUTH_REFRESH_TOKEN, refreshToken);

            // when
            ResultActions resultActions = getResultActions(request, refreshCookie);

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
                    logoutRequestFixture.withAccessToken("invalid.access.token").build();
            Cookie refreshCookie =
                    authCookieTestHelper.createRefreshTokenCookie(AUTH_REFRESH_TOKEN, refreshToken);

            // when
            ResultActions resultActions = getResultActions(request, refreshCookie);

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
        @DisplayName("리프레시 토큰이 null 혹은 비어있을 경우 BAD REQUEST를 반환한다.")
        void shouldReturnBadRequestWhenRefreshTokenIsNullOrBlank() throws Exception {
            // given
            LogoutRequest request = logoutRequestFixture.withAccessToken(accessToken).build();
            Cookie nullCookie =
                    authCookieTestHelper.createRefreshTokenCookie(
                            "null.refresh.cookie", refreshToken);

            // when
            ResultActions resultActions = getResultActions(request, nullCookie);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.MISSING_REFRESH_TOKEN.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.MISSING_REFRESH_TOKEN.getMessage()));
        }

        @Test
        @DisplayName("리프레시 토큰이 존재하지 않거나 위조된 경우 401 UNAUTHORIZED를 반환한다.")
        void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {
            // given
            LogoutRequest request = logoutRequestFixture.withAccessToken(accessToken).build();
            Cookie invalidRefreshTokenCookie =
                    authCookieTestHelper.createRefreshTokenCookie(
                            AUTH_REFRESH_TOKEN, "invalid.refresh.token");

            // when
            ResultActions resultActions = getResultActions(request, invalidRefreshTokenCookie);

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
            LogoutRequest request = logoutRequestFixture.withAccessToken(accessToken).build();
            Cookie refreshCookie =
                    authCookieTestHelper.createRefreshTokenCookie(AUTH_REFRESH_TOKEN, refreshToken);

            // when
            ResultActions resultActions = getResultActions(request, refreshCookie);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }
    }
}
