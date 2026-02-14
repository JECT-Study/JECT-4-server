package com.ject.studytrip.auth.presentation.controller

import com.ject.studytrip.BaseIntegrationTest
import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.domain.repository.KakaoSignupProfileRedisRepository
import com.ject.studytrip.auth.domain.repository.RefreshTokenRedisRepository
import com.ject.studytrip.auth.fixture.KakaoLoginRequestFixture
import com.ject.studytrip.auth.fixture.KakaoSignupRequestFixture
import com.ject.studytrip.auth.fixture.KakaoTokenResponseFixture
import com.ject.studytrip.auth.fixture.KakaoUserInfoResponseFixture
import com.ject.studytrip.auth.fixture.LogoutRequestFixture
import com.ject.studytrip.auth.helper.AuthCookieTestHelper
import com.ject.studytrip.auth.helper.KakaoOauthTestHelper
import com.ject.studytrip.auth.helper.TokenTestHelper
import com.ject.studytrip.auth.infra.provider.KakaoOauthProvider
import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest
import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest
import com.ject.studytrip.auth.presentation.dto.request.LogoutRequest
import com.ject.studytrip.global.common.constants.CookieConstants.AUTH_REFRESH_TOKEN
import com.ject.studytrip.global.common.constants.CookieConstants.OAUTH_SIGNUP_KEY
import com.ject.studytrip.member.domain.error.MemberErrorCode
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.helper.MemberTestHelper
import jakarta.servlet.http.Cookie
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration

@DisplayName("AuthController 통합 테스트")
class AuthControllerIntegrationTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var memberTestHelper: MemberTestHelper

    @Autowired private lateinit var tokenTestHelper: TokenTestHelper

    @Autowired private lateinit var kakaoOauthTestHelper: KakaoOauthTestHelper

    @Autowired private lateinit var authCookieTestHelper: AuthCookieTestHelper

    @Autowired private lateinit var refreshTokenRedisRepository: RefreshTokenRedisRepository

    @Autowired private lateinit var kakaoSignupProfileRedisRepository: KakaoSignupProfileRedisRepository

    @MockitoBean private lateinit var kakaoOauthProvider: KakaoOauthProvider

    private lateinit var member: Member
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var signupKey: String

    @BeforeEach
    fun setUp() {
        member = memberTestHelper.saveMember()
        accessToken = tokenTestHelper.createAccessToken(member.id.toString(), member.role.name)
        refreshToken = tokenTestHelper.createRefreshToken()
        signupKey = kakaoSignupProfileRedisRepository.saveAndIssueSignupKey(member.socialId, member.email, member.profileImage)

        refreshTokenRedisRepository.saveRefreshToken(member.id.toString(), refreshToken, Duration.ofSeconds(30).toMillis())
    }

    companion object {
        private const val BASE_AUTH_URL = "/api/auth"
        private const val TEST_ORIGIN = "http://localhost:8080"
        private const val RESPONSE_COOKIE_NAME = "Set-Cookie"
    }

    @Nested
    @DisplayName("카카오 회원가입 API")
    inner class KakaoSignup {
        private val fixture = KakaoSignupRequestFixture()

        private fun getResultActions(
            request: KakaoSignupRequest,
            cookie: Cookie,
        ): ResultActions =
            mockMvc.perform(
                post("$BASE_AUTH_URL/signup/kakao")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request))
                    .cookie(cookie),
            )

        @Test
        @DisplayName("회원가입 요청 시 signupKey 쿠키가 존재하지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenSignupKeyCookieDoesNotExist() {
            // given
            memberTestHelper.deleteMemberById(member.id)
            val request = fixture.build()
            val cookie = authCookieTestHelper.createKakaoSignupProfileCookie("NULL_COOKIE", signupKey)

            // when
            val resultActions = getResultActions(request, cookie)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.MISSING_KAKAO_SIGNUP_KEY.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.MISSING_KAKAO_SIGNUP_KEY.message))
        }

        @Test
        @DisplayName("회원가입 요청 시 signupKey가 유효하지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenSignupKeyIsInvalid() {
            // given
            memberTestHelper.deleteMemberById(member.id)
            val request = fixture.build()
            val cookie = authCookieTestHelper.createKakaoSignupProfileCookie(OAUTH_SIGNUP_KEY, "invalid.pending.key")

            // when
            val resultActions = getResultActions(request, cookie)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.INVALID_KAKAO_SIGNUP_KEY.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.INVALID_KAKAO_SIGNUP_KEY.message))
        }

        @Test
        @DisplayName("회원가입 요청 시 이미 가입된 사용자라면 409 Conflict를 반환한다.")
        fun shouldReturnConflictWhenMemberAlreadyExists() {
            // given
            val request = fixture.build()
            val cookie = authCookieTestHelper.createKakaoSignupProfileCookie(OAUTH_SIGNUP_KEY, signupKey)

            // when
            val resultActions = getResultActions(request, cookie)

            resultActions
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MemberErrorCode.MEMBER_ALREADY_EXISTS.status.value()))
                .andExpect(jsonPath("$.data.message").value(MemberErrorCode.MEMBER_ALREADY_EXISTS.message))
        }

        @Test
        @DisplayName("회원가입 성공 시 토큰이 발급된다.")
        fun shouldReturnTokenResponseWhenSignupIsSuccessful() {
            // given
            memberTestHelper.deleteMemberById(member.id)
            val request = fixture.build()
            val cookie = authCookieTestHelper.createKakaoSignupProfileCookie(OAUTH_SIGNUP_KEY, signupKey)

            // when
            val resultActions = getResultActions(request, cookie)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data.signupRequired").value(false))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
                .andExpect(header().exists(RESPONSE_COOKIE_NAME))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("$AUTH_REFRESH_TOKEN=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=None")))
        }
    }

    @Nested
    @DisplayName("카카오 로그인 API")
    inner class KakaoLogin {
        private val kakaoLoginRequestFixture = KakaoLoginRequestFixture()
        private val kakaoTokenResponseFixture = KakaoTokenResponseFixture()
        private val kakaoUserInfoResponseFixture = KakaoUserInfoResponseFixture()

        private fun getResultActions(request: KakaoLoginRequest): ResultActions =
            mockMvc.perform(
                post("$BASE_AUTH_URL/login/kakao")
                    .header("Origin", TEST_ORIGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

        @Test
        @DisplayName("탈퇴한 사용자가 인가 코드로 로그인 시 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenMemberAlreadyDeleted() {
            // given
            member.updateDeletedAt()
            val request = kakaoLoginRequestFixture.build()
            val kakaoTokenResponse = kakaoTokenResponseFixture.build()
            kakaoOauthTestHelper.mockThrowException(kakaoTokenResponse, MemberErrorCode.MEMBER_ALREADY_DELETED)

            // when
            val resultActions = getResultActions(request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MemberErrorCode.MEMBER_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MemberErrorCode.MEMBER_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("가입되지 않은 사용자 인가 코드로 로그인 시 회원가입 필요 응답을 반환한다.")
        fun shouldReturnSignupRequiredWhenMemberDoesNotExist() {
            // given
            memberTestHelper.deleteMemberById(member.id)
            val request = kakaoLoginRequestFixture.build()
            val kakaoTokenResponse = kakaoTokenResponseFixture.build()
            val kakaoUserInfoResponse = kakaoUserInfoResponseFixture.build()
            kakaoOauthTestHelper.mockSuccess(kakaoTokenResponse, kakaoUserInfoResponse)

            // when
            val resultActions = getResultActions(request)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data.signupRequired").value(true))
                .andExpect(jsonPath("$.data.accessToken").doesNotExist())
                .andExpect(header().exists(RESPONSE_COOKIE_NAME))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("$OAUTH_SIGNUP_KEY=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=None")))
        }

        @Test
        @DisplayName("가입된 사용자의 인가 코드로 로그인하면 토큰이 발급된다.")
        fun shouldReturnTokenResponseWhenLoginIsSuccessful() {
            // given
            val request = kakaoLoginRequestFixture.build()
            val kakaoTokenResponse = kakaoTokenResponseFixture.build()
            val kakaoUserInfoResponse = kakaoUserInfoResponseFixture.build()
            kakaoOauthTestHelper.mockSuccess(kakaoTokenResponse, kakaoUserInfoResponse)

            // when
            val resultActions = getResultActions(request)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data.signupRequired").value(false))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
                .andExpect(header().exists(RESPONSE_COOKIE_NAME))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("$AUTH_REFRESH_TOKEN=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=None")))
        }
    }

    @Nested
    @DisplayName("토큰 재발급 API")
    inner class ReissueToken {
        private fun getResultActions(cookie: Cookie): ResultActions = mockMvc.perform(post("$BASE_AUTH_URL/token/reissue").cookie(cookie))

        @Test
        @DisplayName("리프레시 토큰이 null이거나 비어있다면 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenRefreshTokenIsNullOrBlank() {
            // given
            val cookie = authCookieTestHelper.createRefreshTokenCookie("null.refresh.cookie", refreshToken)

            // when
            val resultActions = getResultActions(cookie)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.MISSING_REFRESH_TOKEN.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.MISSING_REFRESH_TOKEN.message))
        }

        @Test
        @DisplayName("리프레시 토큰이 존재하지 않거나 위조되었다면 401 UNAUTHORIZED를 반환한다.")
        fun shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() {
            // given
            val cookie = authCookieTestHelper.createRefreshTokenCookie(AUTH_REFRESH_TOKEN, "invalid:refresh:cookie")

            // when
            val resultActions = getResultActions(cookie)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.INVALID_REFRESH_TOKEN.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.INVALID_REFRESH_TOKEN.message))
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 새로운 엑세스 토큰과 리프레시 토큰을 재발급한다.")
        fun shouldReissueTokenWhenRequestIsValid() {
            // given
            val cookie = authCookieTestHelper.createRefreshTokenCookie(AUTH_REFRESH_TOKEN, refreshToken)

            // when
            val resultActions = getResultActions(cookie)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
                .andExpect(header().exists(RESPONSE_COOKIE_NAME))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("$AUTH_REFRESH_TOKEN=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=None")))
        }
    }

    @Nested
    @DisplayName("로그아웃 API")
    inner class Logout {
        private val fixture = LogoutRequestFixture()

        private fun getResultActions(
            request: LogoutRequest,
            cookie: Cookie,
        ): ResultActions =
            mockMvc.perform(
                post("$BASE_AUTH_URL/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .cookie(cookie),
            )

        @Test
        @DisplayName("엑세스 토큰이 존재하지 않거나 위조된 경우 401 UNAUTHORIZED를 반환한다.")
        fun shouldReturnUnauthorizedWhenAccessTokenIsInvalid() {
            // given
            val request = fixture.withAccessToken("invalid.access.token").build()
            val cookie = authCookieTestHelper.createRefreshTokenCookie(AUTH_REFRESH_TOKEN, refreshToken)

            // when
            val resultActions = getResultActions(request, cookie)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.INVALID_JWT_TOKEN.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.INVALID_JWT_TOKEN.message))
        }

        @Test
        @DisplayName("리프레시 토큰이 null 혹은 비어있을 경우 BAD REQUEST를 반환한다.")
        fun shouldReturnBadRequestWhenRefreshTokenIsNullOrBlank() {
            // given
            val request = fixture.withAccessToken(accessToken).build()
            val cookie = authCookieTestHelper.createRefreshTokenCookie("null.refresh.cookie", refreshToken)

            // when
            val resultActions = getResultActions(request, cookie)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.MISSING_REFRESH_TOKEN.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.MISSING_REFRESH_TOKEN.message))
        }

        @Test
        @DisplayName("리프레시 토큰이 존재하지 않거나 위조된 경우 401 UNAUTHORIZED를 반환한다.")
        fun shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() {
            // given
            val request = fixture.withAccessToken(accessToken).build()
            val cookie = authCookieTestHelper.createRefreshTokenCookie(AUTH_REFRESH_TOKEN, "invalid.refresh.token")

            // when
            val resultActions = getResultActions(request, cookie)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.INVALID_REFRESH_TOKEN.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.INVALID_REFRESH_TOKEN.message))
        }

        @Test
        @DisplayName("유효한 요청이 들어오면, 엑세스 토큰을 블랙리스트에 추가하고, 저장된 리프레시 토큰을 제거합니다.")
        fun shouldLogoutWhenRequestIsValid() {
            // given
            val request = fixture.withAccessToken(accessToken).build()
            val cookie = authCookieTestHelper.createRefreshTokenCookie(AUTH_REFRESH_TOKEN, refreshToken)

            // when
            val resultActions = getResultActions(request, cookie)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }
}
