package com.ject.studytrip.member.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ject.studytrip.BaseIntegrationTest;
import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.fixture.TokenFixture;
import com.ject.studytrip.auth.helper.TokenTestHelper;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.UpdateMemberRequestFixture;
import com.ject.studytrip.member.helper.MemberTestHelper;
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.helper.TripTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("MemberController 통합 테스트")
class MemberControllerIntegrationTest extends BaseIntegrationTest {
    private static final String BASE_MEMBER_URL = "/api/members";

    @Autowired private MemberTestHelper memberTestHelper;
    @Autowired private TokenTestHelper tokenTestHelper;
    @Autowired private TripTestHelper tripTestHelper;

    private Member member;
    private String accessToken;

    @BeforeEach
    void setUp() {
        member = memberTestHelper.saveMember();
        accessToken =
                tokenTestHelper.createAccessToken(
                        member.getId().toString(), member.getRole().name());
        tripTestHelper.saveTrip(member, TripCategory.COURSE);
        tripTestHelper.saveTrip(member, TripCategory.COURSE);
        tripTestHelper.saveTrip(member, TripCategory.COURSE);
        tripTestHelper.saveTrip(member, TripCategory.EXPLORE);
        tripTestHelper.saveTrip(member, TripCategory.EXPLORE);
    }

    @Nested
    @DisplayName("멤버 수정 API")
    class UpdateMember {
        private final UpdateMemberRequestFixture fixture = new UpdateMemberRequestFixture();

        private ResultActions getResultActions(String accessToken, UpdateMemberRequest request)
                throws Exception {
            return mockMvc.perform(
                    patch(BASE_MEMBER_URL + "/me")
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // given
            UpdateMemberRequest request = fixture.withNickname("새로운 닉네임").build();

            // when
            ResultActions resultActions = getResultActions("", request);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getMessage()));
        }

        @Test
        @DisplayName("삭제된 여행일 경우 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenMemberAlreadyDeleted() throws Exception {
            // given
            UpdateMemberRequest request = fixture.withNickname("새로운 닉네임").build();
            member.updateDeletedAt();

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 멤버 닉네임을 수정한다.")
        void shouldUpdateMemberNicknameWhenRequestIsValid() throws Exception {
            // given
            UpdateMemberRequest request = fixture.withNickname("새로운 닉네임").build();

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 멤버 카테고리를 수정한다.")
        void shouldUpdateMemberCategoryWhenRequestIsValid() throws Exception {
            // given
            UpdateMemberRequest request = fixture.withCategory("WORKER").build();

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 멤버 닉네임과 카테고리를 수정한다.")
        void shouldUpdateMemberNicknameAndCategoryWhenRequestIsValid() throws Exception {
            // given
            UpdateMemberRequest request =
                    fixture.withNickname("새로운 닉네임").withCategory("WORKER").build();

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }
    }

    @Nested
    @DisplayName("멤버 삭제 API")
    class DeleteMember {
        private ResultActions getResultActions(String accessToken) throws Exception {
            return mockMvc.perform(
                    delete(BASE_MEMBER_URL + "/me")
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken)
                            .contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // when
            ResultActions resultActions = getResultActions("");

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getMessage()));
        }

        @Test
        @DisplayName("삭제된 여행일 경우 404 Not Found를 반환한다.")
        void shouldReturnBadRequestWhenMemberAlreadyDeleted() throws Exception {
            // given
            member.updateDeletedAt();

            // when
            ResultActions resultActions = getResultActions(accessToken);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("유효한 멤버 ID가 들어오면 멤버를 삭제한다.")
        void shouldDeleteMemberWhenMemberIdIsValid() throws Exception {
            // when
            ResultActions resultActions = getResultActions(accessToken);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }
    }

    @Nested
    @DisplayName("멤버 상세 조회 API")
    class LoadMemberDetail {
        private ResultActions getResultActions(String accessToken) throws Exception {
            return mockMvc.perform(
                    get(BASE_MEMBER_URL + "/me")
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken)
                            .contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // when
            ResultActions resultActions = getResultActions("");

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getMessage()));
        }

        @Test
        @DisplayName("삭제된 여행일 경우 404 Not Found를 반환한다.")
        void shouldReturnBadRequestWhenMemberAlreadyDeleted() throws Exception {
            // given
            member.updateDeletedAt();

            // when
            ResultActions resultActions = getResultActions(accessToken);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("유효한 멤버 ID가 들어오면 멤버 상세 정보를 반환한다.")
        void shouldReturnMemberDetailWhenMemberIdIsValid() throws Exception {
            // when
            ResultActions resultActions = getResultActions(accessToken);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.data.memberId").value(member.getId()))
                    .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                    .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                    .andExpect(jsonPath("$.data.profileImage").value(member.getProfileImage()))
                    .andExpect(jsonPath("$.data.category").value(member.getCategory().name()))
                    .andExpect(jsonPath("$.data.courseTripCount").value(3))
                    .andExpect(jsonPath("$.data.exploreTripCount").value(2))
                    .andExpect(jsonPath("$.data.studyLogCount").value(0));
        }
    }
}
