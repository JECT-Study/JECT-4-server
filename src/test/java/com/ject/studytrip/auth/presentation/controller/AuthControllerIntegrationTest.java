package com.ject.studytrip.auth.presentation.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ject.studytrip.BaseIntegrationTest;
import com.ject.studytrip.auth.fixture.KakaoOauthFixture;
import com.ject.studytrip.auth.infra.provider.KakaoOauthProvider;
import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest;
import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.helper.MemberTestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("AuthController 통합 테스트")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MemberTestHelper memberTestHelper;

    @MockitoBean KakaoOauthProvider kakaoOauthProvider;

    @Nested
    @DisplayName("kakaoLogin 메서드는")
    class KakaoLogin {

        @Test
        @DisplayName("가입되지 않은 사용자 인가 코드로 로그인 시 MEMBER_NEED_SIGNUP 예외가 발생한다")
        void shouldThrowExceptionWhenMemberNotSignUp() throws Exception {
            // given
            KakaoLoginRequest request = KakaoOauthFixture.createLoginRequest();
            given(kakaoOauthProvider.getKakaoTokens(anyString()))
                    .willReturn(KakaoOauthFixture.createTokenResponse());
            given(kakaoOauthProvider.getKakaoUserInfo(anyString()))
                    .willThrow(new CustomException(MemberErrorCode.MEMBER_NEED_SIGNUP));

            // when
            ResultActions result =
                    mockMvc.perform(
                            post("/api/auth/login/kakao")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isConflict())
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
        @DisplayName("가입된 사용자의 인가 코드로 로그인하면 토큰이 발급된다")
        void shouldReturnTokenResponseWhenLoginIsSuccessful() throws Exception {
            // given
            KakaoLoginRequest request = KakaoOauthFixture.createLoginRequest();
            memberTestHelper.saveMember();
            given(kakaoOauthProvider.getKakaoTokens(anyString()))
                    .willReturn(KakaoOauthFixture.createTokenResponse());
            given(kakaoOauthProvider.getKakaoUserInfo(anyString()))
                    .willReturn(KakaoOauthFixture.createKakaoUserInfoResponse());

            // when
            ResultActions result =
                    mockMvc.perform(
                            post("/api/auth/login/kakao")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("kakaoSignup 메서드는")
    class KakaoSignup {

        @Test
        @DisplayName("이미 가입된 사용자가 회원가입 요청 시 MEMBER_ALREADY_EXISTS 예외가 발생한다")
        void shouldThrowExceptionWhenSignupForExistingMember() throws Exception {
            // given
            KakaoSignupRequest request = KakaoOauthFixture.createSignupRequest();
            memberTestHelper.saveMember();
            given(kakaoOauthProvider.getKakaoTokens(anyString()))
                    .willReturn(KakaoOauthFixture.createTokenResponse());
            given(kakaoOauthProvider.getKakaoUserInfo(anyString()))
                    .willReturn(KakaoOauthFixture.createKakaoUserInfoResponse());

            // when
            ResultActions result =
                    mockMvc.perform(
                            post("/api/auth/signup/kakao")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isConflict())
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
        @DisplayName("회원가입 요청 시 category가 유효하지 않으면 MEMBER_CATEGORY_REQUIRED 예외가 발생한다")
        void shouldThrowExceptionWhenCategoryIsInvalid() throws Exception {
            // given
            KakaoSignupRequest request = new KakaoSignupRequest("valid-code", "", "민우");
            given(kakaoOauthProvider.getKakaoTokens(anyString()))
                    .willReturn(KakaoOauthFixture.createTokenResponse());
            given(kakaoOauthProvider.getKakaoUserInfo(anyString()))
                    .willReturn(KakaoOauthFixture.createKakaoUserInfoResponse());

            // when
            ResultActions result =
                    mockMvc.perform(
                            post("/api/auth/signup/kakao")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            MemberErrorCode.MEMBER_CATEGORY_REQUIRED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("회원가입 요청 시 닉네임이 비어있으면 MEMBER_NICKNAME_REQUIRED 예외가 발생한다")
        void shouldThrowExceptionWhenSignupNicknameIsBlank() throws Exception {
            // given
            KakaoSignupRequest request = new KakaoSignupRequest("valid-code", "STUDENT", "");
            given(kakaoOauthProvider.getKakaoTokens(anyString()))
                    .willReturn(KakaoOauthFixture.createTokenResponse());
            given(kakaoOauthProvider.getKakaoUserInfo(anyString()))
                    .willReturn(KakaoOauthFixture.createKakaoUserInfoResponse());

            // when
            ResultActions result =
                    mockMvc.perform(
                            post("/api/auth/signup/kakao")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            MemberErrorCode.MEMBER_NICKNAME_REQUIRED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("회원가입 요청 시 유효한 정보라면 토큰이 발급된다")
        void shouldReturnTokenResponseWhenSignupIsSuccessful() throws Exception {
            // given
            KakaoSignupRequest request = KakaoOauthFixture.createSignupRequest();
            given(kakaoOauthProvider.getKakaoTokens(anyString()))
                    .willReturn(KakaoOauthFixture.createTokenResponse());
            given(kakaoOauthProvider.getKakaoUserInfo(anyString()))
                    .willReturn(KakaoOauthFixture.createKakaoUserInfoResponse());

            // when
            ResultActions result =
                    mockMvc.perform(
                            post("/api/auth/signup/kakao")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
        }
    }
}
