package com.ject.studytrip.studylog.presentation.controller

import com.ject.studytrip.BaseIntegrationTest
import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.fixture.TokenFixture
import com.ject.studytrip.auth.helper.TokenTestHelper
import com.ject.studytrip.global.exception.error.CommonErrorCode
import com.ject.studytrip.image.domain.error.ImageErrorCode
import com.ject.studytrip.image.infra.s3.provider.S3ImageStorageProvider
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
import com.ject.studytrip.pomodoro.helper.PomodoroTestHelper
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.helper.StampTestHelper
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.fixture.ConfirmStudyLogImageRequestFixture
import com.ject.studytrip.studylog.fixture.CreateStudyLogRequestFixture
import com.ject.studytrip.studylog.fixture.PresignStudyLogImageRequestFixture
import com.ject.studytrip.studylog.helper.StudyLogDailyMissionTestHelper
import com.ject.studytrip.studylog.helper.StudyLogTestHelper
import com.ject.studytrip.studylog.presentation.dto.request.ConfirmStudyLogImageRequest
import com.ject.studytrip.studylog.presentation.dto.request.CreateStudyLogRequest
import com.ject.studytrip.studylog.presentation.dto.request.PresignStudyLogImageRequest
import com.ject.studytrip.trip.domain.error.DailyGoalErrorCode
import com.ject.studytrip.trip.domain.error.TripErrorCode
import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.helper.DailyGoalTestHelper
import com.ject.studytrip.trip.helper.TripTestHelper
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("StudyLogController 통합 테스트")
class StudyLogControllerIntegrationTest : BaseIntegrationTest() {
    @Autowired private lateinit var memberTestHelper: MemberTestHelper

    @Autowired private lateinit var tokenTestHelper: TokenTestHelper

    @Autowired private lateinit var tripTestHelper: TripTestHelper

    @Autowired private lateinit var stampTestHelper: StampTestHelper

    @Autowired private lateinit var missionTestHelper: MissionTestHelper

    @Autowired private lateinit var dailyGoalTestHelper: DailyGoalTestHelper

    @Autowired private lateinit var dailyMissionTestHelper: DailyMissionTestHelper

    @Autowired private lateinit var studyLogTestHelper: StudyLogTestHelper

    @Autowired private lateinit var studyLogDailyMissionTestHelper: StudyLogDailyMissionTestHelper

    @Autowired private lateinit var pomodoroTestHelper: PomodoroTestHelper

    @MockitoBean private lateinit var s3ImageStorageProvider: S3ImageStorageProvider

    private lateinit var member: Member
    private lateinit var token: String
    private lateinit var trip: Trip
    private lateinit var stamp: Stamp
    private lateinit var mission: Mission
    private lateinit var dailyGoal: DailyGoal
    private lateinit var dailyMission: DailyMission
    private lateinit var studyLog: StudyLog

    companion object {
        private const val DEFAULT_PAGE: String = "0"
        private const val DEFAULT_SIZE: String = "5"
        private const val ORDER_LATEST: String = "LATEST"
        private const val ORDER_OLDEST: String = "OLDEST"
    }

    @BeforeEach
    fun setUp() {
        member = memberTestHelper.saveMember()
        token = tokenTestHelper.createAccessToken(member.id.toString(), MemberRole.ROLE_USER.name)
        trip = tripTestHelper.saveTrip(member, TripCategory.COURSE)
        stamp = stampTestHelper.saveStamp(trip, 1)
        mission = missionTestHelper.saveMission(stamp)
        dailyGoal = dailyGoalTestHelper.saveDailyGoal(trip)
        dailyMission = dailyMissionTestHelper.saveDailyMission(mission, dailyGoal)
        pomodoroTestHelper.savePomodoro(dailyGoal)
        studyLog = studyLogTestHelper.saveStudyLog(member, dailyGoal)
    }

    @Nested
    @DisplayName("학습 로그 생성 API")
    inner class CreateStudyLog {
        private val fixture = CreateStudyLogRequestFixture()

        private fun getResultActions(
            token: String,
            tripId: Any,
            dailyGoalId: Any,
            request: CreateStudyLogRequest,
        ): ResultActions =
            mockMvc.perform(
                post("/api/trips/{tripId}/daily-goals/{dailyGoalId}/study-logs", tripId, dailyGoalId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture.authorization(token))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // given
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id)).build()

            // when
            val resultActions = getResultActions("", trip.id, dailyGoal.id, request)

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
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, tripId, dailyGoal.id, request)

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
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, trip.id, dailyGoalId, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("요청이 유효하지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenRequestIsInvalid() {
            // given
            val request =
                fixture
                    .withTotalFocusTimeInMinutes(-30)
                    .withSelectedDailyMissionIds(listOf())
                    .build()

            // when
            val resultActions = getResultActions(token, trip.id, dailyGoal.id, request)

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
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, tripId, dailyGoal.id, request)

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
            val newMember = memberTestHelper.saveMember("test@gmail.com", "test")
            val newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE)
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, newTrip.id, dailyGoal.id, request)

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
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, deletedTrip.id, dailyGoal.id, request)

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
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, completedTrip.id, dailyGoal.id, request)

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
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, trip.id, dailyGoalId, request)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND.message))
        }

        @Test
        @DisplayName("데일리 목표가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenDailyGoalDoesNotBelongToTrip() {
            // given
            val newTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE)
            val newDailyGoal = dailyGoalTestHelper.saveDailyGoal(newTrip)
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, trip.id, newDailyGoal.id, request)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyGoalErrorCode.DAILY_GOAL_NOT_BELONG_TO_TRIP.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyGoalErrorCode.DAILY_GOAL_NOT_BELONG_TO_TRIP.message))
        }

        @Test
        @DisplayName("데일리 목표가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenDailyGoalAlreadyDeleted() {
            // given
            val deletedDailyGoal = dailyGoalTestHelper.saveDeletedDailyGoal(trip)
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, trip.id, deletedDailyGoal.id, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("선택한 데일리 미션 ID 목록 중 존재하지 않는 값이 포함되어 있다면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenSelectedDailyMissionIdsContainInvalidId() {
            // given
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id, 1000L)).build()

            // when
            val resultActions = getResultActions(token, trip.id, dailyGoal.id, request)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyMissionErrorCode.DAILY_MISSION_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyMissionErrorCode.DAILY_MISSION_NOT_FOUND.message))
        }

        @Test
        @DisplayName("데일리 미션이 요청한 데일리 목표에 속하지 않으면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenDailyMissionDoesNotBelongToDailyGoal() {
            // given
            val newDailyGoal = dailyGoalTestHelper.saveDailyGoal(trip)
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, trip.id, newDailyGoal.id, request)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyMissionErrorCode.DAILY_MISSION_NOT_BELONG_TO_DAILY_GOAL.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyMissionErrorCode.DAILY_MISSION_NOT_BELONG_TO_DAILY_GOAL.message))
        }

        @Test
        @DisplayName("데일리 미션이 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenDailyMissionAlreadyDeleted() {
            // given
            val deletedDailyMission = dailyMissionTestHelper.saveDeletedDailyMission(mission, dailyGoal)
            val request = fixture.withSelectedDailyMissionIds(listOf(deletedDailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, trip.id, dailyGoal.id, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(DailyMissionErrorCode.DAILY_MISSION_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(DailyMissionErrorCode.DAILY_MISSION_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("특정 데일리 목표에 속한 뽀모도로가 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenPomodoroForDailyGoalDoesNotExist() {
            // given
            val newDailyGoal = dailyGoalTestHelper.saveDailyGoal(trip)
            val dailyMission = dailyMissionTestHelper.saveDailyMission(mission, newDailyGoal)
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, trip.id, newDailyGoal.id, request)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(PomodoroErrorCode.POMODORO_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(PomodoroErrorCode.POMODORO_NOT_FOUND.message))
        }

        @Test
        @DisplayName("특정 데일리 목표에 속한 뽀모도로가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenPomodoroForDailyGoalAlreadyDeleted() {
            // given
            val newDailyGoal = dailyGoalTestHelper.saveDailyGoal(trip)
            val newDailyMission = dailyMissionTestHelper.saveDailyMission(mission, newDailyGoal)
            pomodoroTestHelper.saveDeletedPomodoro(newDailyGoal)
            val request = fixture.withSelectedDailyMissionIds(listOf(newDailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, trip.id, newDailyGoal.id, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(PomodoroErrorCode.POMODORO_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(PomodoroErrorCode.POMODORO_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("선택한 미션 목록에 이미 삭제된 미션이 포함되어 있다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenSelectedMissionAlreadyDeleted() {
            // given
            val deletedMission = missionTestHelper.saveDeletedMission(stamp)
            val newDailyMission = dailyMissionTestHelper.saveDailyMission(deletedMission, dailyGoal)
            val request = fixture.withSelectedDailyMissionIds(listOf(newDailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, trip.id, dailyGoal.id, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("선택한 미션 목록에 이미 완료된 미션이 포함되어 있다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenSelectedMissionAlreadyCompleted() {
            // given
            val completedMission = missionTestHelper.saveCompletedMission(stamp)
            val newDailyMission = dailyMissionTestHelper.saveDailyMission(completedMission, dailyGoal)
            val request = fixture.withSelectedDailyMissionIds(listOf(newDailyMission.id)).build()

            // when
            val resultActions = getResultActions(token, trip.id, dailyGoal.id, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MissionErrorCode.MISSION_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MissionErrorCode.MISSION_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 학습 로그를 생성하고 반환한다.")
        fun shouldReturnStudyLogWhenRequestIsValid() {
            // given
            val request = fixture.withSelectedDailyMissionIds(listOf(dailyMission.id)).build()
            val initialCompletedMissions = stamp.completedMissions

            // when
            val resultActions = getResultActions(token, trip.id, dailyGoal.id, request)

            // then
            resultActions
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studyLogId").isNumber)

            // 스탬프의 완료된 미션 수 증가 확인
            val updatedStamp = stampTestHelper.getStamp(stamp.id)
            assertThat(updatedStamp.completedMissions).isEqualTo(initialCompletedMissions + 1)
        }

        @Test
        @DisplayName("여러 미션을 선택하면 스탬프의 완료된 미션 수가 증가한다.")
        fun shouldUpdateCompletedMissionsWhenMultipleMissionsSelected() {
            // given
            val newMission = missionTestHelper.saveMission(stamp)
            val newDailyMission = dailyMissionTestHelper.saveDailyMission(newMission, dailyGoal)
            val request =
                fixture
                    .withSelectedDailyMissionIds(listOf(dailyMission.id, newDailyMission.id))
                    .build()
            val initialCompletedMissions = stamp.completedMissions

            // when
            val resultActions = getResultActions(token, trip.id, dailyGoal.id, request)

            // then
            resultActions
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studyLogId").isNumber)

            // 스탬프의 완료된 미션 수 증가 확인
            val updatedStamp = stampTestHelper.getStamp(stamp.id)
            assertThat(updatedStamp.completedMissions).isEqualTo(initialCompletedMissions + 2)
        }
    }

    @Nested
    @DisplayName("학습 로그 목록 조회 API")
    inner class ListStudyLogs {
        private fun getResultActions(
            token: String,
            tripId: Any,
            page: String,
            size: String,
            order: String,
        ): ResultActions =
            mockMvc.perform(
                get("/api/trips/{tripId}/study-logs", tripId)
                    .param("page", page)
                    .param("size", size)
                    .param("order", order)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture.authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", trip.id, DEFAULT_PAGE, DEFAULT_SIZE, ORDER_LATEST)

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
            val resultActions = getResultActions(token, tripId, DEFAULT_PAGE, DEFAULT_SIZE, ORDER_LATEST)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("Request Param 페이징 데이터 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenPagingParameterTypeMismatch() {
            // given
            val page = "abc"
            val size = "abc"

            // when
            val resultActions = getResultActions(token, trip.id, page, size, ORDER_LATEST)

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
            val resultActions = getResultActions(token, trip.id, page, size, ORDER_LATEST)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.CONSTRAINT_VIOLATION.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.CONSTRAINT_VIOLATION.message))
        }

        @Test
        @DisplayName("여행이 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenTripDoesNotExist() {
            // given
            val tripId = -1L

            // when
            val resultActions = getResultActions(token, tripId, DEFAULT_PAGE, DEFAULT_SIZE, ORDER_LATEST)

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
            val newMember = memberTestHelper.saveMember("test@gmail.com", "test")
            val newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE)
            // when
            val resultActions = getResultActions(token, newTrip.id, DEFAULT_PAGE, DEFAULT_SIZE, ORDER_LATEST)

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
            val resultActions = getResultActions(token, deletedTrip.id, DEFAULT_PAGE, DEFAULT_SIZE, ORDER_LATEST)

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
            val resultActions = getResultActions(token, completedTrip.id, DEFAULT_PAGE, DEFAULT_SIZE, ORDER_LATEST)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_COMPLETED.message))
        }

        @Test
        @DisplayName("order 파라미터가 유효하지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenOrderIsInvalid() {
            // given
            val order = "INVALID"

            // when
            val resultActions = getResultActions(token, trip.id, DEFAULT_PAGE, DEFAULT_SIZE, order)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.CONSTRAINT_VIOLATION.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.CONSTRAINT_VIOLATION.message))
        }

        @Test
        @DisplayName("order 파라미터를 LATEST로 지정하면, 최신순으로 정렬된 학습 로그 목록을 슬라이스 처리하여 반환한다.")
        fun shouldReturnStudyLogSliceByTripOrderedByLatest() {
            // given
            studyLogDailyMissionTestHelper.saveStudyLogDailyMissions(studyLog, dailyMission)

            // when
            val resultActions = getResultActions(token, trip.id, DEFAULT_PAGE, DEFAULT_SIZE, ORDER_LATEST)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studyLogs").isNotEmpty)
                .andExpect(jsonPath("$.data.studyLogs[0].studyLogId").value(studyLog.id))
                .andExpect(jsonPath("$.data.studyLogs[0].dailyMissions").isNotEmpty)
                .andExpect(jsonPath("$.data.hasNext").value(false))
        }

        @Test
        @DisplayName("order 파라미터를 OLDEST로 지정하면, 과거순으로 정렬된 학습 로그 목록을 슬라이스 처리하여 반환한다.")
        fun shouldReturnStudyLogSliceByTripOrderedByOldest() {
            // given
            studyLogDailyMissionTestHelper.saveStudyLogDailyMissions(studyLog, dailyMission)

            // when
            val resultActions = getResultActions(token, trip.id, DEFAULT_PAGE, DEFAULT_SIZE, ORDER_OLDEST)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studyLogs").isNotEmpty)
                .andExpect(jsonPath("$.data.studyLogs[0].studyLogId").value(studyLog.id))
                .andExpect(jsonPath("$.data.studyLogs[0].dailyMissions").isNotEmpty)
                .andExpect(jsonPath("$.data.hasNext").value(false))
        }
    }

    @Nested
    @DisplayName("학습 로그 이미지 Presigned URL 발급 API")
    inner class IssuePresignedUrl {
        private val fixture = PresignStudyLogImageRequestFixture()

        private fun getResultActions(
            token: String,
            studyLogId: Long,
            request: PresignStudyLogImageRequest,
        ): ResultActions =
            mockMvc.perform(
                post("/api/study-logs/{studyLogId}/images/presigned", studyLogId)
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
            val resultActions = getResultActions("", studyLog.id, request)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("파일명이 비어있으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenFilenameIsEmpty() {
            // given
            val request = fixture.withOriginFilename("").build()

            // when
            val resultActions = getResultActions(token, studyLog.id, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.message))
        }

        @Test
        @DisplayName("유효하지 않은 확장자는 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenExtensionIsInvalid() {
            // given
            val request = fixture.withOriginFilename("test.pdf").build()

            // when
            val resultActions = getResultActions(token, studyLog.id, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(ImageErrorCode.INVALID_IMAGE_EXTENSION.status.value()))
                .andExpect(jsonPath("$.data.message").value(ImageErrorCode.INVALID_IMAGE_EXTENSION.message))
        }

        @Test
        @DisplayName("파일명이 유효하면 Presigned URL을 발급한다.")
        fun shouldIssuePresignedUrlWhenFilenameIsValid() {
            // given
            val request = fixture.build()
            given(s3ImageStorageProvider.issuePresignedUrl(anyString())).willReturn("https://mocked-presigned-url.com")

            // when
            val resultActions = getResultActions(token, studyLog.id, request)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.presignedUrl").isNotEmpty)
                .andExpect(jsonPath("$.data.tmpKey").isNotEmpty)
                .andExpect(jsonPath("$.data.tmpKey").value(Matchers.startsWith("tmp/study-logs/")))
                .andExpect(jsonPath("$.data.tmpKey").value(Matchers.containsString(studyLog.id.toString())))

            // S3Provider 호출 검증
            verify(s3ImageStorageProvider).issuePresignedUrl(anyString())
        }
    }

    @Nested
    @DisplayName("학습 로그 이미지 확정 API")
    inner class ConfirmImage {
        private val fixture = ConfirmStudyLogImageRequestFixture()

        private fun getResultActions(
            token: String,
            studyLogId: Long,
            request: ConfirmStudyLogImageRequest,
        ): ResultActions =
            mockMvc.perform(
                post("/api/study-logs/{studyLogId}/images/confirm", studyLogId)
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
            val resultActions = getResultActions("", studyLog.id, request)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("tmpKey가 비어있으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTmpKeyIsEmpty() {
            // given
            val request = fixture.withTmpKey("").build()

            // when
            val resultActions = getResultActions(token, studyLog.id, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.message))
        }
    }
}
