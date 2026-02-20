package com.ject.studytrip.mission.presentation.controller

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
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.mission.fixture.CreateMissionRequestFixture
import com.ject.studytrip.mission.fixture.UpdateMissionRequestFixture
import com.ject.studytrip.mission.helper.MissionTestHelper
import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest
import com.ject.studytrip.stamp.domain.error.StampErrorCode
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.helper.StampTestHelper
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("MissionController 통합 테스트")
class MissionControllerIntegrationTest : BaseIntegrationTest() {
    @Autowired private lateinit var memberTestHelper: MemberTestHelper

    @Autowired private lateinit var tokenTestHelper: TokenTestHelper

    @Autowired private lateinit var tripTestHelper: TripTestHelper

    @Autowired private lateinit var stampTestHelper: StampTestHelper

    @Autowired private lateinit var missionTestHelper: MissionTestHelper

    private lateinit var member: Member
    private lateinit var token: String

    // 코스형
    private lateinit var courseTrip: Trip
    private lateinit var courseStamp: Stamp
    private lateinit var courseMission: Mission

    // 탐험형
    private lateinit var exploreTrip: Trip
    private lateinit var exploreStamp: Stamp
    private lateinit var exploreMission: Mission

    // 새로운 여행
    private lateinit var newTrip: Trip

    @BeforeEach
    fun setUp() {
        member = memberTestHelper.saveMember()
        token = tokenTestHelper.createAccessToken(member.id.toString(), MemberRole.ROLE_USER.name)

        courseTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE)
        courseStamp = stampTestHelper.saveStamp(courseTrip, 1)
        courseMission = missionTestHelper.saveMission(courseStamp)

        exploreTrip = tripTestHelper.saveTrip(member, TripCategory.EXPLORE)
        exploreStamp = stampTestHelper.saveStamp(exploreTrip, 0)
        exploreMission = missionTestHelper.saveMission(exploreStamp)

        val newMember = memberTestHelper.saveNewMember("test@gmail.com", "test")
        newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE)
    }

    companion object {
        private const val BASE_MISSION_URL = "/api/trips/{tripId}/stamps/{stampId}/missions"
    }

    @Nested
    @DisplayName("미션 생성 API")
    inner class CreateMission {
        private val fixture = CreateMissionRequestFixture()

        private fun getResultActions(
            token: String,
            tripId: Any,
            stampId: Any,
            request: CreateMissionRequest,
        ): ResultActions =
            mockMvc.perform(
                post(BASE_MISSION_URL, tripId, stampId)
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
            val resultActions = getResultActions("", courseTrip.id.requireId(), courseStamp.id.requireId(), request)

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
            val resultActions = getResultActions(token, tripId, courseStamp.id.requireId(), request)

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
        @DisplayName("CreateMissionRequest 이름이 비어있으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenRequestNameIsBlank() {
            // given
            val request = fixture.withName(" ").build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), request)

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
            val resultActions = getResultActions(token, tripId, courseStamp.id.requireId(), request)

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
            val resultActions = getResultActions(token, newTrip.id.requireId(), courseStamp.id.requireId(), request)

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
            val resultActions = getResultActions(token, deletedTrip.id.requireId(), courseStamp.id.requireId(), request)

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
            val resultActions = getResultActions(token, completedTrip.id.requireId(), courseStamp.id.requireId(), request)

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
            val resultActions = getResultActions(token, courseTrip.id.requireId(), exploreStamp.id.requireId(), request)

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
        @DisplayName("유효한 요청이 들어오면 미션을 생성하고 반환한다.")
        fun shouldCreateAndReturnMissionWhenRequestIsValid() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.data.missionId").isNumber)
        }
    }

    @Nested
    @DisplayName("미션 수정 API")
    inner class UpdateMission {
        private val fixture = UpdateMissionRequestFixture()

        private fun getResultActions(
            token: String,
            tripId: Any,
            stampId: Any,
            missionId: Any,
            request: UpdateMissionRequest,
        ): ResultActions =
            mockMvc.perform(
                patch("$BASE_MISSION_URL/{missionId}", tripId, stampId, missionId)
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
            val resultActions =
                getResultActions("", courseTrip.id.requireId(), courseStamp.id.requireId(), courseMission.id.requireId(), request)

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
            val resultActions = getResultActions(token, tripId, courseStamp.id.requireId(), courseMission.id.requireId(), request)

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
            val resultActions = getResultActions(token, courseTrip.id.requireId(), stampId, courseMission.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("PathVariable 미션 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenMissionIdTypeMismatch() {
            // given
            val missionId = "abc"
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), missionId, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("UpdateMissionRequest 이름이 비어있으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenRequestNameIsBlank() {
            // given
            val request = fixture.withName(" ").build()

            // when
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), courseMission.id.requireId(), request)

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
            val resultActions = getResultActions(token, tripId, courseStamp.id.requireId(), courseMission.id.requireId(), request)

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
            val resultActions =
                getResultActions(token, newTrip.id.requireId(), courseStamp.id.requireId(), courseMission.id.requireId(), request)

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
            val resultActions =
                getResultActions(token, deletedTrip.id.requireId(), courseStamp.id.requireId(), courseMission.id.requireId(), request)

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
            val resultActions =
                getResultActions(token, completedTrip.id.requireId(), courseStamp.id.requireId(), courseMission.id.requireId(), request)

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
            val resultActions = getResultActions(token, courseTrip.id.requireId(), stampId, courseMission.id.requireId(), request)

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
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), exploreStamp.id.requireId(), courseMission.id.requireId(), request)

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
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), deletedStamp.id.requireId(), courseMission.id.requireId(), request)

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
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), completedStamp.id.requireId(), courseMission.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("미션이 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenMissionDoesNotExist() {
            // given
            val missionId = -1L
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), missionId, request)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_NOT_FOUND.message))
        }

        @Test
        @DisplayName("미션이 요청한 스탬프에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenMissionNotBelongToStamp() {
            // given
            val request = fixture.build()

            // when
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), exploreMission.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.message))
        }

        @Test
        @DisplayName("미션이 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenMissionAlreadyDeleted() {
            // given
            val deletedMission = missionTestHelper.saveDeletedMission(courseStamp)
            val request = fixture.build()

            // when
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), deletedMission.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("미션이 이미 완료되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenMissionAlreadyCompleted() {
            // given
            val completedMission = missionTestHelper.saveCompletedMission(courseStamp)
            val request = fixture.build()

            // when
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), completedMission.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 미션 이름을 수정한다.")
        fun shouldUpdateMissionNameWhenRequestIsValid() {
            // given
            val request = fixture.build()

            // when
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), courseMission.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("미션 삭제 API")
    inner class DeleteMission {
        private fun getResultActions(
            token: String,
            tripId: Any,
            stampId: Any,
            missionId: Any,
        ): ResultActions =
            mockMvc.perform(
                delete("$BASE_MISSION_URL/{missionId}", tripId, stampId, missionId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", courseTrip.id.requireId(), courseStamp.id.requireId(), courseMission.id.requireId())

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
            val resultActions = getResultActions(token, tripId, courseStamp.id.requireId(), courseMission.id.requireId())

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
            val resultActions = getResultActions(token, courseTrip.id.requireId(), stampId, courseMission.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("PathVariable 미션 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenMissionIdTypeMismatch() {
            // given
            val missionId = "abc"

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), missionId)

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
            val resultActions = getResultActions(token, tripId, courseStamp.id.requireId(), courseMission.id.requireId())

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
            val resultActions = getResultActions(token, newTrip.id.requireId(), courseStamp.id.requireId(), courseMission.id.requireId())

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
            val resultActions =
                getResultActions(token, deletedTrip.id.requireId(), courseStamp.id.requireId(), courseMission.id.requireId())

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
            val resultActions =
                getResultActions(token, completedTrip.id.requireId(), courseStamp.id.requireId(), courseMission.id.requireId())

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
            val resultActions = getResultActions(token, courseTrip.id.requireId(), stampId, courseMission.id.requireId())

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
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), exploreStamp.id.requireId(), courseMission.id.requireId())

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
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), deletedStamp.id.requireId(), courseMission.id.requireId())

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
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), completedStamp.id.requireId(), courseMission.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("미션이 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenMissionDoesNotExist() {
            // given
            val missionId = -1L

            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), missionId)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_NOT_FOUND.message))
        }

        @Test
        @DisplayName("미션이 요청한 스탬프에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenMissionNotBelongToStamp() {
            // when
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), exploreMission.id.requireId())

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.message))
        }

        @Test
        @DisplayName("미션이 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenMissionAlreadyDeleted() {
            // given
            val deletedMission = missionTestHelper.saveDeletedMission(courseStamp)

            // when
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), deletedMission.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("미션이 이미 완료되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenMissionAlreadyCompleted() {
            // given
            val completedMission = missionTestHelper.saveCompletedMission(courseStamp)

            // when
            val resultActions =
                getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), completedMission.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("특정 미션을 삭제한다.")
        fun shouldDeleteMission() {
            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId(), courseMission.id.requireId())

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("미션 목록 조회 API")
    inner class LoadMissionsByStamp {
        private fun getResultActions(
            token: String,
            tripId: Any,
            stampId: Any,
        ): ResultActions =
            mockMvc.perform(
                get(BASE_MISSION_URL, tripId, stampId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", courseTrip.id.requireId(), courseStamp.id.requireId())

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
            val resultActions = getResultActions(token, tripId, courseStamp.id.requireId())

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
            val resultActions = getResultActions(token, tripId, courseStamp.id.requireId())

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
            val resultActions = getResultActions(token, newTrip.id.requireId(), courseStamp.id.requireId())

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
            val resultActions = getResultActions(token, deletedTrip.id.requireId(), courseStamp.id.requireId())

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
            val resultActions = getResultActions(token, completedTrip.id.requireId(), courseStamp.id.requireId())

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
            val resultActions = getResultActions(token, courseTrip.id.requireId(), exploreStamp.id.requireId())

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
        @DisplayName("특정 스탬프의 미션 목록을 조회하고 반환한다.")
        fun shouldReturnMissionsByStamp() {
            // when
            val resultActions = getResultActions(token, courseTrip.id.requireId(), courseStamp.id.requireId())

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").isNotEmpty)
        }
    }
}
