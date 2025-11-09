package com.ject.studytrip.dummy.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ject.studytrip.BaseIntegrationTest;
import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.fixture.TokenFixture;
import com.ject.studytrip.auth.helper.TokenTestHelper;
import com.ject.studytrip.global.exception.error.CommonErrorCode;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.helper.MemberTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("DummyMissionController 통합 테스트")
class DummyMissionControllerIntegrationTest extends BaseIntegrationTest {
    private static final String BASE_DUMMY_MISSION_URL = "/api/dummies/missions";
    private static final String COURSE_CATEGORY = "COURSE";
    private static final String EXPLORE_CATEGORY = "EXPLORE";
    private static final int COUNT = 10;

    @Autowired private MemberTestHelper memberTestHelper;
    @Autowired private TokenTestHelper tokenTestHelper;

    private Member member;
    private String accessToken;

    @BeforeEach
    void setUp() {
        member = memberTestHelper.saveMember();
        accessToken =
                tokenTestHelper.createAccessToken(
                        member.getId().toString(), member.getRole().name());
    }

    @Nested
    @DisplayName("더미 미션 목록 조회 API")
    class LoadDummyMissions {
        private ResultActions getResultActions(String accessToken, String category, int count)
                throws Exception {
            return mockMvc.perform(
                    get(BASE_DUMMY_MISSION_URL)
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken)
                            .param("category", category)
                            .param("count", String.valueOf(count))
                            .contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // when
            ResultActions resultActions = getResultActions("", COURSE_CATEGORY, COUNT);

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
        @DisplayName("유효하지 않은 카테고리가 들어오면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenCategoryIsInvalid() throws Exception {
            // given
            String invalidCategory = "INVALID";

            // when
            ResultActions resultActions = getResultActions(accessToken, invalidCategory, COUNT);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("유효하지 않은 더미 데이터 개수가 들어오면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenCountIsInvalid() throws Exception {
            // given
            int invalidCount = 0;

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, COURSE_CATEGORY, invalidCount);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("삭제된 사용자일 경우 404 Not Found를 반환한다.")
        void shouldReturnBadRequestWhenMemberAlreadyDeleted() throws Exception {
            // given
            member.updateDeletedAt();

            // when
            ResultActions resultActions = getResultActions(accessToken, COURSE_CATEGORY, COUNT);

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
        @DisplayName("COURSE 카테고리가 들어오면 더미 미션 목록를 반환한다.(DB 저장 X)")
        void shouldReturnDummyMissionsWhenCategoryIsCourse() throws Exception {
            // when
            ResultActions resultActions = getResultActions(accessToken, COURSE_CATEGORY, COUNT);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("EXPLORE 카테고리가 들어오면 더미 미션 목록를 반환한다.(DB 저장 X)")
        void shouldReturnDummyMissionsWhenCategoryIsExplore() throws Exception {
            // when
            ResultActions resultActions = getResultActions(accessToken, EXPLORE_CATEGORY, COUNT);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
