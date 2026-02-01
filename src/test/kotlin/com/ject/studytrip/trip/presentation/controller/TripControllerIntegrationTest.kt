package com.ject.studytrip.trip.presentation.controller

import com.ject.studytrip.BaseIntegrationTest
import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.fixture.TokenFixture
import com.ject.studytrip.auth.helper.TokenTestHelper
import com.ject.studytrip.global.exception.error.CommonErrorCode
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.MemberRole
import com.ject.studytrip.member.helper.MemberTestHelper
import com.ject.studytrip.stamp.domain.error.StampErrorCode
import com.ject.studytrip.stamp.helper.StampTestHelper
import com.ject.studytrip.trip.domain.error.TripErrorCode
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.fixture.CreateTripRequestFixture
import com.ject.studytrip.trip.fixture.UpdateTripRequestFixture
import com.ject.studytrip.trip.helper.TripTestHelper
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest
import com.ject.studytrip.trip.presentation.dto.request.UpdateTripRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@DisplayName("TripController 통합 테스트")
class TripControllerIntegrationTest : BaseIntegrationTest() {
    @Autowired private lateinit var memberTestHelper: MemberTestHelper

    @Autowired private lateinit var tokenTestHelper: TokenTestHelper

    @Autowired private lateinit var tripTestHelper: TripTestHelper

    @Autowired private lateinit var stampTestHelper: StampTestHelper

    private lateinit var member: Member
    private lateinit var token: String
    private lateinit var courseTrip: Trip

    // 새로운 여행
    private lateinit var newTrip: Trip

    @BeforeEach
    fun setUp() {
        member = memberTestHelper.saveMember()
        token = tokenTestHelper.createAccessToken(member.id.toString(), MemberRole.ROLE_USER.name)
        courseTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE)

        val newMember = memberTestHelper.saveMember("test@gmail.com", "test")
        newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE)
    }

    companion object {
        private const val BASE_TRIP_URL = "/api/trips"
        private const val DEFAULT_PAGE: String = "0"
        private const val DEFAULT_SIZE: String = "5"
    }

    @Nested
    @DisplayName("여행 생성 API")
    inner class CreateTrip {
        private val fixture = CreateTripRequestFixture()

        private fun getResultActions(
            token: String,
            request: CreateTripRequest,
        ): ResultActions =
            mockMvc.perform(
                post(BASE_TRIP_URL)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture.authorization(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions("", request)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("CreateMissionRequest 카테고리가 유효하지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenRequestCategoryIsInvalid() {
            // given
            val request = fixture.withCategory("TEST").build()

            // when
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.message))
        }

        @Test
        @DisplayName("CreateMissionRequest 종료일이 과거이면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenRequestEndDateIsInThePast() {
            // given
            val request = fixture.withEndDate(LocalDate.now().minusDays(10)).build()

            // when
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.message))
        }

        @Test
        @DisplayName("여행 카테고리가 COURSE이지만, 종료일이 null이라면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenRequestEndDateIsNullForCourseTrip() {
            // given
            val request = fixture.withCategory(TripCategory.COURSE.name).withEndDate(null).build()

            // when
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.COURSE_TRIP_END_DATE_REQUIRED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.COURSE_TRIP_END_DATE_REQUIRED.message))
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 코스형 여행을 생성하고 반환한다.")
        fun shouldCreateAndReturnCourseTripWhenRequestIsValid() {
            // given
            val request = fixture.build()

            // then
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tripId").isNumber)
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 탐험형 여행을 생성하고 반환한다.")
        fun shouldCreateAndReturnExploreTripWhenRequestIsValid() {
            // given
            val request = fixture.withCategory(TripCategory.EXPLORE.name).withEndDate(null).build()

            // then
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.data.tripId").isNumber)
        }
    }

    @Nested
    @DisplayName("여행 수정 API")
    inner class UpdateTrip {
        private val fixture = UpdateTripRequestFixture()

        private fun getResultActions(
            token: String,
            tripId: Any,
            request: UpdateTripRequest,
        ): ResultActions =
            mockMvc.perform(
                patch("$BASE_TRIP_URL/{tripId}", tripId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture.authorization(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions("", courseTrip.id, request)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripIdTypeMismatch() {
            // given
            val tripId = "abc"
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, tripId, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("UpdateMissionRequest 카테고리가 유효하지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenRequestCategoryIsInvalid() {
            // given
            val request = fixture.withCategory("TEST").build()

            // when
            val resultActions = getResultActions(token, courseTrip.id, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.message))
        }

        @Test
        @DisplayName("UpdateMissionRequest 종료일이 과거이면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenRequestEndDateIsInThePast() {
            // given
            val request = fixture.withEndDate(LocalDate.now().minusDays(10)).build()

            // when
            val resultActions = getResultActions(token, courseTrip.id, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.message))
        }

        @Test
        @DisplayName("여행이 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenTripDoesNotExist() {
            // given
            val tripId = -1L
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, tripId, request)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_NOT_FOUND.message))
        }

        @Test
        @DisplayName("여행의 소유자가 아니라면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenMemberIsNotTripOwner() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, newTrip.id, request)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.NOT_TRIP_OWNER.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.NOT_TRIP_OWNER.message))
        }

        @Test
        @DisplayName("여행이 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripAlreadyDeleted() {
            // given
            val deletedTrip = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE)
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, deletedTrip.id, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("여행이 이미 완료되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripAlreadyCompleted() {
            // given
            val completedTrip = tripTestHelper.saveCompletedTrip(member, TripCategory.COURSE)
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, completedTrip.id, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 여행을 수정한다.")
        fun shouldUpdateTripWhenRequestIsValid() {
            // given
            val request = fixture.withEndDate(LocalDate.now().plusDays(3)).build()

            // when
            val resultActions = getResultActions(token, courseTrip.id, request)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("여행 삭제 API")
    inner class DeleteTrip {
        private fun getResultActions(
            token: String,
            tripId: Any,
        ): ResultActions =
            mockMvc.perform(
                delete("$BASE_TRIP_URL/{tripId}", tripId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture.authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", courseTrip.id)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripIdTypeMismatch() {
            // given
            val tripId = "abc"

            // when
            val resultActions = getResultActions(token, tripId)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("여행이 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenTripDoesNotExist() {
            // given
            val tripId = -1L

            // when
            val resultActions = getResultActions(token, tripId)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_NOT_FOUND.message))
        }

        @Test
        @DisplayName("여행의 소유자가 아니라면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenMemberIsNotTripOwner() {
            // when
            val resultActions = getResultActions(token, newTrip.id)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.NOT_TRIP_OWNER.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.NOT_TRIP_OWNER.message))
        }

        @Test
        @DisplayName("여행이 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripAlreadyDeleted() {
            // given
            val deletedTrip = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE)

            // when
            val resultActions = getResultActions(token, deletedTrip.id)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("여행이 이미 완료되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripAlreadyCompleted() {
            // given
            val completedTrip = tripTestHelper.saveCompletedTrip(member, TripCategory.COURSE)

            // when
            val resultActions = getResultActions(token, completedTrip.id)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("특정 여행을 삭제한다.")
        fun shouldDeleteTrip() {
            // when
            val resultActions = getResultActions(token, courseTrip.id)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("여행 완료 API")
    inner class CompleteTrip {
        private fun getResultActions(
            token: String,
            tripId: Any,
        ): ResultActions =
            mockMvc.perform(
                patch("$BASE_TRIP_URL/{tripId}/complete", tripId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture.authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", courseTrip.id)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripIdTypeMismatch() {
            // given
            val tripId = "abc"

            // when
            val resultActions = getResultActions(token, tripId)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("여행이 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenTripDoesNotExist() {
            // given
            val tripId = -1L

            // when
            val resultActions = getResultActions(token, tripId)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_NOT_FOUND.message))
        }

        @Test
        @DisplayName("여행의 소유자가 아니라면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenMemberIsNotTripOwner() {
            // when
            val resultActions = getResultActions(token, newTrip.id)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.NOT_TRIP_OWNER.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.NOT_TRIP_OWNER.message))
        }

        @Test
        @DisplayName("여행이 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripAlreadyDeleted() {
            // given
            val deletedTrip = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE)

            // when
            val resultActions = getResultActions(token, deletedTrip.id)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("여행이 이미 완료되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripAlreadyCompleted() {
            // given
            val completedTrip = tripTestHelper.saveCompletedTrip(member, TripCategory.COURSE)

            // when
            val resultActions = getResultActions(token, completedTrip.id)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("특정 여행의 어떤 스탬프가 완료되지 않았다면 예외가 발생한다.")
        fun shouldReturnBadRequestWhenAnyStampIsNotCompleted() {
            // given
            stampTestHelper.saveStamp(courseTrip, 1)

            // when
            val resultActions = getResultActions(token, courseTrip.id)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.ALL_STAMPS_NOT_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.ALL_STAMPS_NOT_COMPLETED.message))
        }

        @Test
        @DisplayName("특정 여행의 모든 스탬프가 완료되었다면 여행을 완료한다.")
        fun shouldCompleteStampWhenAllStampsAreCompleted() {
            // given
            stampTestHelper.saveCompletedStamp(courseTrip, 1)
            stampTestHelper.saveCompletedStamp(courseTrip, 2)

            // when
            val resultActions = getResultActions(token, courseTrip.id)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("여행 카테고리 목록 조회 API")
    inner class LoadTripCategories {
        private fun getResultActions(): ResultActions = mockMvc.perform(get("$BASE_TRIP_URL/categories"))

        @Test
        @DisplayName("여행 카테고리 목록을 조회하고 반환한다.")
        fun shouldReturnTripCategories() {
            // when
            val resultActions = getResultActions()

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").isNotEmpty)
        }
    }

    @Nested
    @DisplayName("여행 목록 조회 API")
    inner class LoadTrips {
        private fun getResultActions(
            token: String,
            page: String,
            size: String,
        ): ResultActions =
            mockMvc.perform(
                get(BASE_TRIP_URL)
                    .param("page", page)
                    .param("size", size)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture.authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", DEFAULT_PAGE, DEFAULT_SIZE)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("Request Param 페이징 데이터 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenPagingParameterTypeMismatch() {
            // given
            val page = "abc"
            val size = "abc"

            // when
            val resultActions = getResultActions(token, page, size)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("Request Param 페이징 데이터가 유효하지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenPagingParameterIsInvalid() {
            // given
            val page = "-1"
            val size = "-1"

            // when
            val resultActions = getResultActions(token, page, size)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.CONSTRAINT_VIOLATION.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.CONSTRAINT_VIOLATION.message))
        }

        @Test
        @DisplayName("여행 목록을 슬라이스 처리하여 반환한다.")
        fun shouldReturnTripSliceByMember() {
            // when
            val resultActions = getResultActions(token, DEFAULT_PAGE, DEFAULT_SIZE)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data.tripInfos").isNotEmpty)
                .andExpect(jsonPath("$.data.hasNext").value(false))
        }
    }

    @Nested
    @DisplayName("여행 상세 조회 API")
    inner class LoadTrip {
        private fun getResultActions(
            token: String,
            tripId: Any,
        ): ResultActions =
            mockMvc.perform(
                get("$BASE_TRIP_URL/{tripId}", tripId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture.authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", courseTrip.id)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripIdTypeMismatch() {
            // given
            val tripId = "abc"

            // when
            val resultActions = getResultActions(token, tripId)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("여행이 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenTripDoesNotExist() {
            // given
            val tripId = -1L

            // when
            val resultActions = getResultActions(token, tripId)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_NOT_FOUND.message))
        }

        @Test
        @DisplayName("여행의 소유자가 아니라면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenMemberIsNotTripOwner() {
            // when
            val resultActions = getResultActions(token, newTrip.id)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.NOT_TRIP_OWNER.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.NOT_TRIP_OWNER.message))
        }

        @Test
        @DisplayName("여행이 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripAlreadyDeleted() {
            // given
            val deletedTrip = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE)

            // when
            val resultActions = getResultActions(token, deletedTrip.id)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("여행이 이미 완료되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripAlreadyCompleted() {
            // given
            val completedTrip = tripTestHelper.saveCompletedTrip(member, TripCategory.COURSE)

            // when
            val resultActions = getResultActions(token, completedTrip.id)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("특정 여행을 상세 조회합니다.")
        fun shouldReturnTrip() {
            // when
            val resultActions = getResultActions(token, courseTrip.id)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").isNotEmpty)
        }
    }
}
