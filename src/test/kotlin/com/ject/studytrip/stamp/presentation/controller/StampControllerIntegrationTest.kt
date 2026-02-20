package com.ject.studytrip.stamp.presentation.controller

import com.ject.studytrip.BaseIntegrationTest
import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.fixture.TokenFixture
import com.ject.studytrip.auth.helper.TokenTestHelper
import com.ject.studytrip.global.exception.error.CommonErrorCode
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.MemberRole
import com.ject.studytrip.member.helper.MemberTestHelper
import com.ject.studytrip.mission.domain.error.MissionErrorCode
import com.ject.studytrip.mission.helper.MissionTestHelper
import com.ject.studytrip.stamp.domain.error.StampErrorCode
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.fixture.CreateStampRequestFixture
import com.ject.studytrip.stamp.fixture.UpdateStampOrderRequestFixture
import com.ject.studytrip.stamp.fixture.UpdateStampRequestFixture
import com.ject.studytrip.stamp.helper.StampTestHelper
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest
import com.ject.studytrip.trip.domain.error.TripErrorCode
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.helper.TripTestHelper
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("StampController 통합 테스트")
class StampControllerIntegrationTest : BaseIntegrationTest() {
    @Autowired private lateinit var memberTestHelper: MemberTestHelper

    @Autowired private lateinit var tokenTestHelper: TokenTestHelper

    @Autowired private lateinit var tripTestHelper: TripTestHelper

    @Autowired private lateinit var stampTestHelper: StampTestHelper

    @Autowired private lateinit var missionTestHelper: MissionTestHelper

    private lateinit var member: Member
    private lateinit var token: String

    // 코스형
    private lateinit var courseTrip: Trip
    private lateinit var courseStamp1: Stamp
    private lateinit var courseStamp2: Stamp

    // 탐험형
    private lateinit var exploreTrip: Trip
    private lateinit var exploreStamp1: Stamp
    private lateinit var exploreStamp2: Stamp

    // 새로운 여행
    private lateinit var newTrip: Trip

    @BeforeEach
    fun setUp() {
        member = memberTestHelper.saveMember()
        token = tokenTestHelper.createAccessToken(member.id.toString(), MemberRole.ROLE_USER.name)

        courseTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE)
        courseStamp1 = stampTestHelper.saveStamp(courseTrip, 1)
        courseStamp2 = stampTestHelper.saveStamp(courseTrip, 2)

        exploreTrip = tripTestHelper.saveTrip(member, TripCategory.EXPLORE)
        exploreStamp1 = stampTestHelper.saveStamp(exploreTrip, 0)
        exploreStamp2 = stampTestHelper.saveStamp(exploreTrip, 0)

        val newMember = memberTestHelper.saveNewMember("test@gmail.com", "test")
        newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE)
    }

    companion object {
        private const val BASE_STAMP_URL = "/api/trips/{tripId}/stamps"
    }

    @Nested
    @DisplayName("스탬프 생성 API")
    inner class CreateStamp {
        private val fixture = CreateStampRequestFixture()

        private fun getResultActions(
            token: String,
            tripId: Any,
            request: CreateStampRequest,
        ): ResultActions =
            mockMvc.perform(
                post(BASE_STAMP_URL, tripId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions("", courseTrip.id.requireId(), request)

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
        @DisplayName("CreateStampRequest 이름이 비어있으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenRequestNameIsBlank() {
            // given
            val request = fixture.withName(" ").build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), request)

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
            val resultActions = getResultActions(token, newTrip.id.requireId(), request)

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
            val resultActions = getResultActions(token, deletedTrip.id.requireId(), request)

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
            val resultActions = getResultActions(token, completedTrip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 스탬프를 생성하고 반환한다.")
        fun shouldCreateAndReturnStampWhenRequestIsValid() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stampId").isNumber)
        }
    }

    @Nested
    @DisplayName("스탬프 수정 API")
    inner class UpdateStamp {
        private val fixture = UpdateStampRequestFixture()

        private fun getResultActions(
            token: String,
            tripId: Any,
            stampId: Any,
            request: UpdateStampRequest,
        ): ResultActions =
            mockMvc.perform(
                patch("$BASE_STAMP_URL/{stampId}", tripId, stampId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions("", courseTrip.id.requireId(), courseStamp1.id.requireId(), request)

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
            val resultActions = getResultActions(token, tripId, courseStamp1.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("PathVariable 스탬프 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampIdTypeMismatch() {
            // given
            val stampId = "abc"
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), stampId, request)

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
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, tripId, courseStamp1.id.requireId(), request)

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
            val resultActions = getResultActions(token, newTrip.id.requireId(), courseStamp1.id.requireId(), request)

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
            val resultActions = getResultActions(token, deletedTrip.id.requireId(), courseStamp1.id.requireId(), request)

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
            val resultActions = getResultActions(token, completedTrip.id.requireId(), courseStamp1.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("스탬프가 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenStampDoesNotExist() {
            // given
            val stampId = -1L
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), stampId, request)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_NOT_FOUND.message))
        }

        @Test
        @DisplayName("스탬프가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenStampNotBelongToTrip() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), exploreStamp1.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.message))
        }

        @Test
        @DisplayName("스탬프가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampAlreadyDeleted() {
            // given
            val deletedStamp = stampTestHelper.saveDeletedStamp(courseTrip, 3)
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), deletedStamp.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("스탬프가 이미 완료되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampAlreadyCompleted() {
            // given
            val completedStamp = stampTestHelper.saveCompletedStamp(courseTrip, 3)
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), completedStamp.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 스탬프를 수정한다.")
        fun shouldUpdateStampWhenRequestIsValid() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), courseStamp1.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("스탬프 순서 변경 API")
    inner class UpdateStampOrders {
        private val fixture = UpdateStampOrderRequestFixture()

        private fun getResultActions(
            token: String,
            tripId: Any,
            request: UpdateStampOrderRequest,
        ): ResultActions =
            mockMvc.perform(
                put("$BASE_STAMP_URL/orders", tripId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions("", courseTrip.id.requireId(), request)

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
            val resultActions = getResultActions(token, newTrip.id.requireId(), request)

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
            val resultActions = getResultActions(token, deletedTrip.id.requireId(), request)

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
            val resultActions = getResultActions(token, completedTrip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("탐험형 여행이 스탬프 순서를 변경한다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampOrdersAreUpdatedForExploreTrip() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, exploreTrip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.CANNOT_UPDATE_ORDER_FOR_EXPLORATION_TRIP.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.CANNOT_UPDATE_ORDER_FOR_EXPLORATION_TRIP.message))
        }

        @Test
        @DisplayName("UpdateOrderRequest에 존재하지 않는 스탬프 ID가 포함되어 있다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenRequestContainsNonExistentStampId() {
            // given
            val request = fixture.withOrderedStampIds(listOf(1000L, 2000L)).build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.INVALID_STAMP_ID_IN_REQUEST.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.INVALID_STAMP_ID_IN_REQUEST.message))
        }

        @Test
        @DisplayName("스탬프가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenStampNotBelongToTrip() {
            // given
            val request =
                fixture
                    .withOrderedStampIds(
                        listOf(courseStamp1.id.requireId(), courseStamp2.id.requireId(), exploreStamp1.id.requireId()),
                    ).build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.message))
        }

        @Test
        @DisplayName("스탬프가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampAlreadyDeleted() {
            // given
            val deletedStamp = stampTestHelper.saveDeletedStamp(courseTrip, 3)
            val request =
                fixture
                    .withOrderedStampIds(
                        listOf(courseStamp1.id.requireId(), courseStamp2.id.requireId(), deletedStamp.id.requireId()),
                    ).build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("스탬프가 이미 완료되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampAlreadyCompleted() {
            // given
            val completedStamp = stampTestHelper.saveCompletedStamp(courseTrip, 3)
            val request =
                fixture
                    .withOrderedStampIds(
                        listOf(courseStamp1.id.requireId(), courseStamp2.id.requireId(), completedStamp.id.requireId()),
                    ).build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 스탬프 순서를 변경한다.")
        fun shouldUpdateStampOrdersWhenRequestIsValid() {
            // given
            val request = fixture.withOrderedStampIds(listOf(courseStamp2.id.requireId(), courseStamp1.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("스탬프 삭제 API")
    inner class DeleteStamp {
        private fun getResultActions(
            token: String,
            tripId: Any,
            stampId: Any,
        ): ResultActions =
            mockMvc.perform(
                delete("$BASE_STAMP_URL/{stampId}", tripId, stampId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", courseTrip.id.requireId(), courseStamp1.id.requireId())

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
            val resultActions = getResultActions(token, tripId, courseStamp1.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("PathVariable 스탬프 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampIdTypeMismatch() {
            // given
            val stampId = "abc"

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), stampId)

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
            val resultActions = getResultActions(token, tripId, courseStamp1.id.requireId())

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
            val resultActions = getResultActions(token, newTrip.id.requireId(), courseStamp1.id.requireId())

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
            val resultActions = getResultActions(token, deletedTrip.id.requireId(), courseStamp1.id.requireId())

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
            val resultActions = getResultActions(token, completedTrip.id.requireId(), courseStamp1.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("스탬프가 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenStampDoesNotExist() {
            // given
            val stampId = -1L

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), stampId)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_NOT_FOUND.message))
        }

        @Test
        @DisplayName("스탬프가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenStampNotBelongToTrip() {
            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), exploreStamp1.id.requireId())

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.message))
        }

        @Test
        @DisplayName("스탬프가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampAlreadyDeleted() {
            // given
            val deletedStamp = stampTestHelper.saveDeletedStamp(courseTrip, 3)

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), deletedStamp.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("스탬프가 이미 완료되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampAlreadyCompleted() {
            // given
            val completedStamp = stampTestHelper.saveCompletedStamp(courseTrip, 3)

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), completedStamp.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("특정 스탬프를 삭제한다.")
        fun shouldDeleteStamp() {
            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), courseStamp1.id.requireId())

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("스탬프 완료 API")
    inner class CompleteStamp {
        private fun getResultActions(
            token: String,
            tripId: Any,
            stampId: Any,
        ): ResultActions =
            mockMvc.perform(
                patch("$BASE_STAMP_URL/{stampId}/complete", tripId, stampId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", courseTrip.id.requireId(), courseStamp1.id.requireId())

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
            val resultActions = getResultActions(token, tripId, courseStamp1.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("PathVariable 스탬프 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampIdTypeMismatch() {
            // given
            val stampId = "abc"

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), stampId)

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
            val resultActions = getResultActions(token, tripId, courseStamp1.id.requireId())

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
            val resultActions = getResultActions(token, newTrip.id.requireId(), courseStamp1.id.requireId())

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
            val resultActions = getResultActions(token, deletedTrip.id.requireId(), courseStamp1.id.requireId())

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
            val resultActions = getResultActions(token, completedTrip.id.requireId(), courseStamp1.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("스탬프가 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenStampDoesNotExist() {
            // given
            val stampId = -1L

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), stampId)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_NOT_FOUND.message))
        }

        @Test
        @DisplayName("스탬프가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenStampNotBelongToTrip() {
            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), exploreStamp1.id.requireId())

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.message))
        }

        @Test
        @DisplayName("스탬프가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampAlreadyDeleted() {
            // given
            val deletedStamp = stampTestHelper.saveDeletedStamp(courseTrip, 3)

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), deletedStamp.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("스탬프가 이미 완료되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampAlreadyCompleted() {
            // given
            val completedStamp = stampTestHelper.saveCompletedStamp(courseTrip, 3)

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), completedStamp.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("특정 스탬프의 어떤 미션이 완료되지 않았다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenAnyMissionIsNotCompleted() {
            // given
            missionTestHelper.saveMission(courseStamp1)
            missionTestHelper.saveCompletedMission(courseStamp1)

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), courseStamp1.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.ALL_MISSIONS_NOT_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.ALL_MISSIONS_NOT_COMPLETED.message))
        }

        @Test
        @DisplayName("특정 스탬프의 모든 미션이 완료되었다면 스탬프를 완료한다.")
        fun shouldCompleteStampWhenAllMissionsAreCompleted() {
            // given
            missionTestHelper.saveCompletedMission(courseStamp1)
            missionTestHelper.saveCompletedMission(courseStamp1)

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), courseStamp1.id.requireId())

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("스탬프 목록 조회 API")
    inner class LoadStampsByTrip {
        private fun getResultActions(
            token: String,
            tripId: Any,
        ): ResultActions =
            mockMvc.perform(
                get(BASE_STAMP_URL, tripId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", courseTrip.id.requireId())

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
            val resultActions = getResultActions(token, newTrip.id.requireId())

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
            val resultActions = getResultActions(token, deletedTrip.id.requireId())

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
            val resultActions = getResultActions(token, completedTrip.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("특정 스탬프의 미션 목록을 조회하고 반환한다.")
        fun shouldReturnStampsByTrip() {
            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId())

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").isNotEmpty)
        }
    }

    @Nested
    @DisplayName("스탬프 상세 조회 API")
    inner class LoadStamp {
        private fun getResultActions(
            token: String,
            tripId: Any,
            stampId: Any,
        ): ResultActions =
            mockMvc.perform(
                get("$BASE_STAMP_URL/{stampId}", tripId, stampId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", courseTrip.id.requireId(), courseStamp1.id.requireId())

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
            val resultActions = getResultActions(token, tripId, courseStamp1.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("PathVariable 스탬프 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampIdTypeMismatch() {
            // given
            val stampId = "abc"

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), stampId)

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
            val resultActions = getResultActions(token, tripId, courseStamp1.id.requireId())

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
            val resultActions = getResultActions(token, newTrip.id.requireId(), courseStamp1.id.requireId())

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
            val resultActions = getResultActions(token, deletedTrip.id.requireId(), courseStamp1.id.requireId())

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
            val resultActions = getResultActions(token, completedTrip.id.requireId(), courseStamp1.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("스탬프가 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenStampDoesNotExist() {
            // given
            val stampId = -1L

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), stampId)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_NOT_FOUND.message))
        }

        @Test
        @DisplayName("스탬프가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenStampNotBelongToTrip() {
            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), exploreStamp1.id.requireId())

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.message))
        }

        @Test
        @DisplayName("스탬프가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampAlreadyDeleted() {
            // given
            val deletedStamp = stampTestHelper.saveDeletedStamp(courseTrip, 3)

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), deletedStamp.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("스탬프가 이미 완료되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStampAlreadyCompleted() {
            // given
            val completedStamp = stampTestHelper.saveCompletedStamp(courseTrip, 3)

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), completedStamp.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("특정 스탬프를 상세 조회하고 반환한다.")
        fun shouldReturnStamp() {
            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), courseStamp1.id.requireId())

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").isNotEmpty)
        }
    }
}
