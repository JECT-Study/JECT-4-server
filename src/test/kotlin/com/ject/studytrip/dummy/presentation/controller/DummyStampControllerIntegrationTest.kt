package com.ject.studytrip.dummy.presentation.controller

import com.ject.studytrip.BaseIntegrationTest
import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.fixture.TokenFixture
import com.ject.studytrip.auth.helper.TokenTestHelper
import com.ject.studytrip.global.exception.error.CommonErrorCode
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.helper.MemberTestHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("DummyStampController 통합 테스트")
class DummyStampControllerIntegrationTest : BaseIntegrationTest() {
    @Autowired private lateinit var memberTestHelper: MemberTestHelper

    @Autowired private lateinit var tokenTestHelper: TokenTestHelper

    private lateinit var member: Member
    private lateinit var token: String

    @BeforeEach
    fun setUp() {
        member = memberTestHelper.saveMember()
        token = tokenTestHelper.createAccessToken(member.id.toString(), member.role.name)
    }

    companion object {
        private const val BASE_DUMMY_STAMP_URL = "/api/dummies/stamps"
        private const val COURSE_CATEGORY = "COURSE"
        private const val EXPLORE_CATEGORY = "EXPLORE"
        private const val DUMMY_STAMP_COUNT = 10
    }

    @Nested
    @DisplayName("더미 스탬프 목록 조회 API")
    inner class LoadDummyMissions {
        private fun getResultActions(
            token: String,
            category: String,
            count: Any,
        ): ResultActions =
            mockMvc.perform(
                get(BASE_DUMMY_STAMP_URL)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token))
                    .param("category", category)
                    .param("count", count.toString())
                    .contentType(MediaType.APPLICATION_JSON),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", COURSE_CATEGORY, DUMMY_STAMP_COUNT)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("Request Param 카테고리 데이터 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenCategoryParameterTypeMismatch() {
            // given
            val category = "INVALID"

            // when
            val resultActions = getResultActions(token, category, DUMMY_STAMP_COUNT)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.CONSTRAINT_VIOLATION.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.CONSTRAINT_VIOLATION.message))
        }

        @Test
        @DisplayName("Request Param 더미 데이터 개수가 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenCountParameterIsInvalid() {
            // given
            val count = -1

            // when
            val resultActions = getResultActions(token, COURSE_CATEGORY, count)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.CONSTRAINT_VIOLATION.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.CONSTRAINT_VIOLATION.message))
        }

        @Test
        @DisplayName("COURSE 카테고리가 들어오면 코스형 더미 스탬프 목록를 반환한다. (DB 저장 X)")
        fun shouldReturnDummyCourseStampsWhenCategoryIsCourse() {
            // when
            val resultActions = getResultActions(token, COURSE_CATEGORY, DUMMY_STAMP_COUNT)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data[0].stampOrder").value(1))
        }

        @Test
        @DisplayName("EXPLORE 카테고리가 들어오면 탐험형 더미 스탬프 목록를 반환한다. (DB 저장 X)")
        fun shouldReturnDummyExploreStampsWhenCategoryIsExplore() {
            // when
            val resultActions = getResultActions(token, EXPLORE_CATEGORY, DUMMY_STAMP_COUNT)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data[0].stampOrder").value(0))
        }
    }
}
