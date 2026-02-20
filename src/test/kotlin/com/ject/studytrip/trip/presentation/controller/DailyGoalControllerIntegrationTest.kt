package com.ject.studytrip.trip.presentation.controller

import com.ject.studytrip.BaseIntegrationTest
import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.fixture.TokenFixture
import com.ject.studytrip.auth.helper.TokenTestHelper
import com.ject.studytrip.global.exception.error.CommonErrorCode
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.MemberRole
import com.ject.studytrip.member.helper.MemberTestHelper
import com.ject.studytrip.mission.domain.error.DailyMissionErrorCode
import com.ject.studytrip.mission.domain.error.MissionErrorCode
import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.mission.helper.DailyMissionTestHelper
import com.ject.studytrip.mission.helper.MissionTestHelper
import com.ject.studytrip.pomodoro.domain.error.PomodoroErrorCode
import com.ject.studytrip.pomodoro.fixture.CreatePomodoroRequestFixture
import com.ject.studytrip.pomodoro.helper.PomodoroTestHelper
import com.ject.studytrip.stamp.domain.error.StampErrorCode
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.helper.StampTestHelper
import com.ject.studytrip.trip.domain.error.DailyGoalErrorCode
import com.ject.studytrip.trip.domain.error.TripErrorCode
import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.fixture.CreateDailyGoalRequestFixture
import com.ject.studytrip.trip.fixture.UpdateDailyGoalRequestFixture
import com.ject.studytrip.trip.helper.DailyGoalTestHelper
import com.ject.studytrip.trip.helper.TripTestHelper
import com.ject.studytrip.trip.presentation.dto.request.CreateDailyGoalRequest
import com.ject.studytrip.trip.presentation.dto.request.UpdateDailyGoalRequest
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

@DisplayName("DailyGoalController 통합 테스트")
class DailyGoalControllerIntegrationTest : BaseIntegrationTest() {
    @Autowired private lateinit var memberTestHelper: MemberTestHelper

    @Autowired private lateinit var tokenTestHelper: TokenTestHelper

    @Autowired private lateinit var tripTestHelper: TripTestHelper

    @Autowired private lateinit var stampTestHelper: StampTestHelper

    @Autowired private lateinit var missionTestHelper: MissionTestHelper

    @Autowired private lateinit var dailyGoalTestHelper: DailyGoalTestHelper

    @Autowired private lateinit var dailyMissionTestHelper: DailyMissionTestHelper

    @Autowired private lateinit var pomodoroTestHelper: PomodoroTestHelper

    private lateinit var member: Member
    private lateinit var token: String
    private lateinit var trip: Trip
    private lateinit var stamp: Stamp
    private lateinit var mission1: Mission
    private lateinit var mission2: Mission
    private lateinit var dailyGoal: DailyGoal
    private lateinit var dailyMission: DailyMission

    // 새로운 여행
    private lateinit var newTrip: Trip

    @BeforeEach
    fun setUp() {
        member = memberTestHelper.saveMember()
        token = tokenTestHelper.createAccessToken(member.id.toString(), MemberRole.ROLE_USER.name)
        trip = tripTestHelper.saveTrip(member, TripCategory.COURSE)
        stamp = stampTestHelper.saveStamp(trip, 1)
        mission1 = missionTestHelper.saveMission(stamp)
        mission2 = missionTestHelper.saveMission(stamp)
        dailyGoal = dailyGoalTestHelper.saveDailyGoal(trip)
        dailyMission = dailyMissionTestHelper.saveDailyMission(mission1, dailyGoal)

        val newMember = memberTestHelper.saveNewMember("test@gmail.com", "test")
        newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE)
    }

    companion object {
        private const val BASE_DAILY_GOAL_URL = "/api/trips/{tripId}/daily-goals"
    }

    @Nested
    @DisplayName("데일리 목표 생성 API")
    inner class CreateDailyGoal {
        private val fixture = CreateDailyGoalRequestFixture()

        private fun getResultActions(
            token: String,
            tripId: Any,
            request: CreateDailyGoalRequest,
        ): ResultActions =
            mockMvc.perform(
                post(BASE_DAILY_GOAL_URL, tripId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // given
            val request = fixture.withMissionIds(listOf(mission1.id.requireId(), mission2.id.requireId())).build()

            // when
            val resultActions = getResultActions("", trip.id.requireId(), request)

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
            val request = fixture.withMissionIds(listOf(mission1.id.requireId(), mission2.id.requireId())).build()

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
        @DisplayName("CreateDailyGoalRequest 뽀모도로 집중 시간이 1분 미만이면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenDailyGoalPomodoroFocusTimeInMinuteIsLessThanOneMinute() {
            // given
            val pomodoro = CreatePomodoroRequestFixture().withFocusDurationInMinute(0).build()
            val request = fixture.withPomodoro(pomodoro).withMissionIds(listOf(mission1.id.requireId(), mission2.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.message))
        }

        @Test
        @DisplayName("CreateDailyGoalRequest 뽀모도로 집중 세션이 1개 미만이면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenDailyGoalPomodoroFocusSessionCountIsLessThanOne() {
            // given
            val pomodoro = CreatePomodoroRequestFixture().withFocusSessionCount(0).build()
            val request = fixture.withPomodoro(pomodoro).withMissionIds(listOf(mission1.id.requireId(), mission2.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), request)

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
            val request = fixture.withMissionIds(listOf(mission1.id.requireId(), mission2.id.requireId())).build()

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
            val request = fixture.withMissionIds(listOf(mission1.id.requireId(), mission2.id.requireId())).build()

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
            val request = fixture.withMissionIds(listOf(mission1.id.requireId(), mission2.id.requireId())).build()

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
            val request = fixture.withMissionIds(listOf(mission1.id.requireId(), mission2.id.requireId())).build()

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
        @DisplayName("어떤 미션이 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenAnyMissionDoesNotExist() {
            // given
            val request = fixture.withMissionIds(listOf(mission1.id.requireId(), -1L)).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_NOT_FOUND.message))
        }

        @Test
        @DisplayName("어떤 미션이 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenAnyMissionAlreadyDeleted() {
            // given
            val deletedMission = missionTestHelper.saveDeletedMission(stamp)
            val request = fixture.withMissionIds(listOf(mission1.id.requireId(), deletedMission.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("어떤 미션이 이미 완료되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenAnyMissionAlreadyCompleted() {
            // given
            val completedMission = missionTestHelper.saveCompletedMission(stamp)
            val request = fixture.withMissionIds(listOf(mission1.id.requireId(), completedMission.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("어떤 미션의 스탬프가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenAnyMissionStampNotBelongToTrip() {
            // given
            val otherTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE)
            stampTestHelper.saveStamp(otherTrip, 2)
            val request = fixture.withMissionIds(listOf(mission1.id.requireId(), mission2.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, otherTrip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.message))
        }

        @Test
        @DisplayName("특정 코스형 여행에서 진행 중인 스탬프가 존재하지 않으면 예외가 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenProgressStampDoesNotExistForCourseTrip() {
            // given
            val otherTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE)
            val completedStamp = stampTestHelper.saveCompletedStamp(otherTrip, 2)
            val otherMission = missionTestHelper.saveMission(completedStamp)
            val request = fixture.withMissionIds(listOf(otherMission.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, otherTrip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_NOT_FOUND.message))
        }

        @Test
        @DisplayName("어떤 미션이 스탬프에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnBadRequestWhenMissionNotBelongToStamp() {
            // given
            val newStamp = stampTestHelper.saveStamp(trip, 2)
            val newMission = missionTestHelper.saveMission(newStamp)
            val request = fixture.withMissionIds(listOf(newMission.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.message))
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 데일리 목표, 뽀모도로, 데일리 미션들을 생성하고, 데일리 목표를 반환한다.")
        fun shouldCreateAndReturnDailyGoalWhenRequestIsValid() {
            // given
            val request = fixture.withMissionIds(listOf(mission1.id.requireId(), mission2.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dailyGoalId").isNumber)
        }
    }

    @Nested
    @DisplayName("데일리 목표 수정 API")
    inner class UpdateDailyGoal {
        private val fixture = UpdateDailyGoalRequestFixture()

        private fun getResultActions(
            token: String,
            tripId: Any,
            dailyGoalId: Any,
            request: UpdateDailyGoalRequest,
        ): ResultActions =
            mockMvc.perform(
                patch("$BASE_DAILY_GOAL_URL/{dailyGoalId}", tripId, dailyGoalId)
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
            val resultActions = getResultActions("", trip.id.requireId(), dailyGoal.id.requireId(), request)

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
            val resultActions = getResultActions(token, tripId, dailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("PathVariable 데일리 목표 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenDailyGoalIdTypeMismatch() {
            // given
            val dailyGoalId = "abc"
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoalId, request)

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
            val resultActions = getResultActions(token, tripId, dailyGoal.id.requireId(), request)

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
            val resultActions = getResultActions(token, newTrip.id.requireId(), dailyGoal.id.requireId(), request)

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
            val resultActions = getResultActions(token, deletedTrip.id.requireId(), dailyGoal.id.requireId(), request)

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
            val resultActions = getResultActions(token, completedTrip.id.requireId(), dailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("데일리 목표가 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenDailyGoalDoesNotExist() {
            // given
            val dailyGoalId = -1L
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoalId, request)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND.message))
        }

        @Test
        @DisplayName("데일리 목표가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenDailyGoalNotBelongToTrip() {
            // given
            val otherTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE)
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, otherTrip.id.requireId(), dailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyGoalErrorCode.DAILY_GOAL_NOT_BELONGS_TO_TRIP.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyGoalErrorCode.DAILY_GOAL_NOT_BELONGS_TO_TRIP.message))
        }

        @Test
        @DisplayName("데일리 목표가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenDailyGoalAlreadyDeleted() {
            // given
            val deletedDailyGoal = dailyGoalTestHelper.saveDeletedDailyGoal(trip)
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), deletedDailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("어떤 데일리 미션이 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenAnyDailyMissionDoesNotExist() {
            // given
            val request = fixture.withDeleteDailyMissionIds(listOf(dailyMission.id.requireId(), -1L)).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyMissionErrorCode.DAILY_MISSION_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyMissionErrorCode.DAILY_MISSION_NOT_FOUND.message))
        }

        @Test
        @DisplayName("어떤 데일리 미션이 요청한 데일리 목표에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenAnyDailyMissionNotBelongToDailyGoal() {
            // given
            val otherDailyGoal = dailyGoalTestHelper.saveDeletedDailyGoal(trip)
            val otherDailyMission = dailyMissionTestHelper.saveDailyMission(mission1, otherDailyGoal)
            val request = fixture.withDeleteDailyMissionIds(listOf(otherDailyMission.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyMissionErrorCode.DAILY_MISSION_NOT_BELONGS_TO_DAILY_GOAL.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyMissionErrorCode.DAILY_MISSION_NOT_BELONGS_TO_DAILY_GOAL.message))
        }

        @Test
        @DisplayName("어떤 데일리 미션이 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenAnyDailyMissionAlreadyDeleted() {
            // given
            val deletedDailyMission = dailyMissionTestHelper.saveDeletedDailyMission(mission1, dailyGoal)
            val request = fixture.withDeleteDailyMissionIds(listOf(dailyMission.id.requireId(), deletedDailyMission.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyMissionErrorCode.DAILY_MISSION_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyMissionErrorCode.DAILY_MISSION_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("어떤 미션이 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenAnyMissionDoesNotExist() {
            // given
            val request = fixture.withAddMissionIds(listOf(mission1.id.requireId(), mission2.id.requireId(), -1L)).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_NOT_FOUND.message))
        }

        @Test
        @DisplayName("어떤 미션이 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenAnyMissionAlreadyDeleted() {
            // given
            val deletedMission = missionTestHelper.saveDeletedMission(stamp)
            val request = fixture.withAddMissionIds(listOf(mission1.id.requireId(), deletedMission.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("어떤 미션이 이미 완료되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenAnyMissionAlreadyCompleted() {
            // given
            val completedMission = missionTestHelper.saveCompletedMission(stamp)
            val request = fixture.withAddMissionIds(listOf(mission1.id.requireId(), completedMission.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("어떤 미션의 스탬프가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenAnyMissionStampNotBelongToTrip() {
            // given
            val otherTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE)
            val otherStamp = stampTestHelper.saveStamp(otherTrip, 2)
            val otherMission = missionTestHelper.saveMission(otherStamp)
            val request = fixture.withAddMissionIds(listOf(otherMission.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.message))
        }

        @Test
        @DisplayName("특정 코스형 여행에서 진행 중인 스탬프가 존재하지 않으면 예외가 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenProgressStampDoesNotExistForCourseTrip() {
            // given
            val otherTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE)
            val completedStamp = stampTestHelper.saveCompletedStamp(otherTrip, 2)
            val otherDailyGoal = dailyGoalTestHelper.saveDailyGoal(otherTrip)
            val otherMission = missionTestHelper.saveMission(completedStamp)
            val request = fixture.withAddMissionIds(listOf(otherMission.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, otherTrip.id.requireId(), otherDailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StampErrorCode.STAMP_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(StampErrorCode.STAMP_NOT_FOUND.message))
        }

        @Test
        @DisplayName("어떤 미션이 스탬프에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnBadRequestWhenMissionNotBelongToStamp() {
            // given
            val newStamp = stampTestHelper.saveStamp(trip, 2)
            val newMission = missionTestHelper.saveMission(newStamp)
            val request = fixture.withAddMissionIds(listOf(newMission.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.message))
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 데일리 목표를 수정한다.")
        fun shouldUpdateDailyGoalWhenRequestIsValid() {
            // given
            val request = fixture.withDeleteDailyMissionIds(listOf(dailyMission.id.requireId())).build()

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId(), request)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("데일리 목표 삭제 API")
    inner class DeleteDailyGoal {
        private fun getResultActions(
            token: String,
            tripId: Any,
            dailyGoalId: Any,
        ): ResultActions =
            mockMvc.perform(
                delete("$BASE_DAILY_GOAL_URL/{dailyGoalId}", tripId, dailyGoalId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", trip.id.requireId(), dailyGoal.id.requireId())

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
            val resultActions = getResultActions(token, tripId, dailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("PathVariable 데일리 목표 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenDailyGoalIdTypeMismatch() {
            // given
            val dailyGoalId = "abc"

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoalId)

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
            val resultActions = getResultActions(token, tripId, dailyGoal.id.requireId())

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
            val resultActions = getResultActions(token, newTrip.id.requireId(), dailyGoal.id.requireId())

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
            val resultActions = getResultActions(token, deletedTrip.id.requireId(), dailyGoal.id.requireId())

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
            val resultActions = getResultActions(token, completedTrip.id.requireId(), dailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("데일리 목표가 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenDailyGoalDoesNotExist() {
            // given
            val dailyGoalId = -1L

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoalId)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND.message))
        }

        @Test
        @DisplayName("데일리 목표가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenDailyGoalNotBelongToTrip() {
            // given
            val newDailyGoal = dailyGoalTestHelper.saveDailyGoal(newTrip)
            pomodoroTestHelper.savePomodoro(newDailyGoal)

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), newDailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyGoalErrorCode.DAILY_GOAL_NOT_BELONGS_TO_TRIP.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyGoalErrorCode.DAILY_GOAL_NOT_BELONGS_TO_TRIP.message))
        }

        @Test
        @DisplayName("데일리 목표가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenDailyGoalAlreadyDeleted() {
            // given
            val deletedDailyGoal = dailyGoalTestHelper.saveDeletedDailyGoal(trip)

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), deletedDailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("뽀모도로가 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenPomodoroDoesNotExist() {
            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(PomodoroErrorCode.POMODORO_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(PomodoroErrorCode.POMODORO_NOT_FOUND.message))
        }

        @Test
        @DisplayName("뽀모도로가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenPomodoroAlreadyDeleted() {
            // given
            pomodoroTestHelper.saveDeletedPomodoro(dailyGoal)

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(PomodoroErrorCode.POMODORO_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(PomodoroErrorCode.POMODORO_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("데일리 목표, 뽀모도로, 데일리 미션들을 삭제한다.")
        fun shouldDeleteDailyGoalAndPomodoroAndDailyMissions() {
            // given
            pomodoroTestHelper.savePomodoro(dailyGoal)

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("데일리 목표 상세 조회 API")
    inner class LoadDailyGoal {
        private fun getResultActions(
            token: String,
            tripId: Any,
            dailyGoalId: Any,
        ): ResultActions =
            mockMvc.perform(
                get("$BASE_DAILY_GOAL_URL/{dailyGoalId}", tripId, dailyGoalId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", trip.id.requireId(), dailyGoal.id.requireId())

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
            val resultActions = getResultActions(token, tripId, dailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("PathVariable 데일리 목표 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenDailyGoalIdTypeMismatch() {
            // given
            val dailyGoalId = "abc"

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoalId)

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
            val resultActions = getResultActions(token, tripId, dailyGoal.id.requireId())

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
            val resultActions = getResultActions(token, newTrip.id.requireId(), dailyGoal.id.requireId())

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
            val resultActions = getResultActions(token, deletedTrip.id.requireId(), dailyGoal.id.requireId())

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
            val resultActions = getResultActions(token, completedTrip.id.requireId(), dailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("데일리 목표가 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenDailyGoalDoesNotExist() {
            // given
            val dailyGoalId = -1L

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoalId)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND.message))
        }

        @Test
        @DisplayName("데일리 목표가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenDailyGoalNotBelongToTrip() {
            // given
            val newDailyGoal = dailyGoalTestHelper.saveDailyGoal(newTrip)
            pomodoroTestHelper.savePomodoro(newDailyGoal)

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), newDailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyGoalErrorCode.DAILY_GOAL_NOT_BELONGS_TO_TRIP.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyGoalErrorCode.DAILY_GOAL_NOT_BELONGS_TO_TRIP.message))
        }

        @Test
        @DisplayName("데일리 목표가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenDailyGoalAlreadyDeleted() {
            // given
            val deletedDailyGoal = dailyGoalTestHelper.saveDeletedDailyGoal(trip)

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), deletedDailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("뽀모도로가 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenPomodoroDoesNotExist() {
            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(PomodoroErrorCode.POMODORO_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(PomodoroErrorCode.POMODORO_NOT_FOUND.message))
        }

        @Test
        @DisplayName("뽀모도로가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenPomodoroAlreadyDeleted() {
            // given
            pomodoroTestHelper.saveDeletedPomodoro(dailyGoal)

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(PomodoroErrorCode.POMODORO_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(PomodoroErrorCode.POMODORO_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("특정 데일리 목표를 상세 조회하고 반환한다.")
        fun shouldReturnDailyGoal() {
            // given
            pomodoroTestHelper.savePomodoro(dailyGoal)

            // when
            val resultActions = getResultActions(token, trip.id.requireId(), dailyGoal.id.requireId())

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNotEmpty)
        }
    }
}
