package com.ject.studytrip.trip.presentation.controller

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
import com.ject.studytrip.mission.helper.DailyMissionTestHelper
import com.ject.studytrip.mission.helper.MissionTestHelper
import com.ject.studytrip.stamp.helper.StampTestHelper
import com.ject.studytrip.studylog.domain.error.StudyLogErrorCode
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.helper.StudyLogDailyMissionTestHelper
import com.ject.studytrip.studylog.helper.StudyLogTestHelper
import com.ject.studytrip.trip.domain.error.TripErrorCode
import com.ject.studytrip.trip.domain.error.TripReportErrorCode
import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.domain.model.TripReport
import com.ject.studytrip.trip.fixture.ConfirmTripReportImageRequestFixture
import com.ject.studytrip.trip.fixture.CreateTripReportRequestFixture
import com.ject.studytrip.trip.fixture.PresignTripReportImageRequestFixture
import com.ject.studytrip.trip.helper.DailyGoalTestHelper
import com.ject.studytrip.trip.helper.TripReportTestHelper
import com.ject.studytrip.trip.helper.TripTestHelper
import com.ject.studytrip.trip.presentation.dto.request.ConfirmTripReportImageRequest
import com.ject.studytrip.trip.presentation.dto.request.CreateTripReportRequest
import com.ject.studytrip.trip.presentation.dto.request.PresignTripReportImageRequest
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("TripReportController 통합 테스트")
class TripReportControllerIntegrationTest : BaseIntegrationTest() {
    @Autowired private lateinit var memberTestHelper: MemberTestHelper

    @Autowired private lateinit var tokenTestHelper: TokenTestHelper

    @Autowired private lateinit var tripTestHelper: TripTestHelper

    @Autowired private lateinit var stampTestHelper: StampTestHelper

    @Autowired private lateinit var missionTestHelper: MissionTestHelper

    @Autowired private lateinit var dailyGoalTestHelper: DailyGoalTestHelper

    @Autowired private lateinit var studyLogTestHelper: StudyLogTestHelper

    @Autowired private lateinit var dailyMissionTestHelper: DailyMissionTestHelper

    @Autowired private lateinit var studyLogDailyMissionTestHelper: StudyLogDailyMissionTestHelper

    @Autowired private lateinit var tripReportTestHelper: TripReportTestHelper

    @MockitoBean lateinit var s3ImageStorageProvider: S3ImageStorageProvider

    private lateinit var member: Member
    private lateinit var token: String
    private lateinit var completedTrip: Trip
    private lateinit var dailyGoal: DailyGoal
    private lateinit var studyLog1: StudyLog
    private lateinit var studyLog2: StudyLog
    private lateinit var tripReport: TripReport

    private lateinit var newMember: Member

    @BeforeEach
    fun setUp() {
        member = memberTestHelper.saveMember()
        token = tokenTestHelper.createAccessToken(member.id.toString(), MemberRole.ROLE_USER.name)
        completedTrip = tripTestHelper.saveCompletedTrip(member, TripCategory.COURSE)
        dailyGoal = dailyGoalTestHelper.saveDailyGoal(completedTrip)
        val stamp = stampTestHelper.saveStamp(completedTrip, 1)
        val mission = missionTestHelper.saveMission(stamp)
        val dailyMission = dailyMissionTestHelper.saveDailyMission(mission, dailyGoal)
        studyLog1 = studyLogTestHelper.saveStudyLog(member, dailyGoal)
        studyLog2 = studyLogTestHelper.saveStudyLog(member, dailyGoal)
        studyLogDailyMissionTestHelper.saveStudyLogDailyMissions(studyLog2, dailyMission)
        tripReport = tripReportTestHelper.saveTripReport(member)

        newMember = memberTestHelper.saveMember("test@gmail.com", "test")
    }

    companion object {
        private const val BASE_TRIP_REPORT_URL = "/api/trip-reports"
        private const val DEFAULT_PAGE: String = "0"
        private const val DEFAULT_SIZE: String = "5"
    }

    @Nested
    @DisplayName("여행 리포트 생성 API")
    inner class CreateTripReport {
        private val fixture = CreateTripReportRequestFixture()

        private fun getResultActions(
            token: String,
            request: CreateTripReportRequest,
        ): ResultActions =
            mockMvc.perform(
                post(BASE_TRIP_REPORT_URL)
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
        @DisplayName("학습 로그 ID 목록 중 존재하지 않는 ID가 존재하면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenAnyStudyLogIdDoesNotExist() {
            // given
            val request = fixture.withStudyLogIds(listOf(studyLog1.id, -1L)).build()

            // when
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StudyLogErrorCode.STUDY_LOG_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(StudyLogErrorCode.STUDY_LOG_NOT_FOUND.message))
        }

        @Test
        @DisplayName("학습 로그 ID 목록 중 이미 삭제된 ID가 존재하면 404 NotFound를 반환한다.")
        fun shouldReturnBadRequestWhenAnyStudyLogAlreadyDeleted() {
            // given
            val deletedStudyLog = studyLogTestHelper.saveDeletedStudyLog(member, dailyGoal)
            val request = fixture.withStudyLogIds(listOf(deletedStudyLog.id, studyLog2.id)).build()

            // when
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(StudyLogErrorCode.STUDY_LOG_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(StudyLogErrorCode.STUDY_LOG_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 여행 리포트를 생성하고 반환한다.")
        fun shouldCreateTripReportWhenRequestIsValid() {
            // given
            val request = fixture.withStudyLogIds(listOf(studyLog1.id, studyLog2.id)).build()

            // when
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.data.tripReportId").isNumber)
        }
    }

    @Nested
    @DisplayName("여행 리포트 삭제 API")
    inner class DeleteTripReport {
        private fun getResultActions(
            token: String,
            tripReportId: Any,
        ): ResultActions =
            mockMvc.perform(
                delete("$BASE_TRIP_REPORT_URL/{tripReportId}", tripReportId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture.authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", tripReport.id)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("PathVariable 여행 리포트 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripReportIdTypeMismatch() {
            // given
            val tripReportId = "abc"

            // when
            val resultActions = getResultActions(token, tripReportId)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("여행 리포트가 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenTripReportDoesNotExist() {
            // given
            val tripReportId = -1L

            // when
            val resultActions = getResultActions(token, tripReportId)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripReportErrorCode.TRIP_REPORT_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripReportErrorCode.TRIP_REPORT_NOT_FOUND.message))
        }

        @Test
        @DisplayName("여행 리포트의 소유자가 아니라면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenMemberIsNotTripReportOwner() {
            // given
            val newTripReport = tripReportTestHelper.saveTripReport(newMember)

            // when
            val resultActions = getResultActions(token, newTripReport.id)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripReportErrorCode.NOT_TRIP_REPORT_OWNER.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripReportErrorCode.NOT_TRIP_REPORT_OWNER.message))
        }

        @Test
        @DisplayName("여행 리포트가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripReportAlreadyDeleted() {
            // given
            val deletedTripReport = tripReportTestHelper.saveDeletedTripReport(member)

            // when
            val resultActions = getResultActions(token, deletedTripReport.id)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripReportErrorCode.TRIP_REPORT_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripReportErrorCode.TRIP_REPORT_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("특정 여행 리포트를 삭제한다.")
        fun shouldDeleteTripReport() {
            // when
            val resultActions = getResultActions(token, tripReport.id)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("여행 리포트 이미지 Presigned URL 발급 API")
    inner class IssuePresignedUrl {
        private val fixture = PresignTripReportImageRequestFixture()

        private fun getResultActions(
            token: String,
            tripReportId: Any,
            request: PresignTripReportImageRequest,
        ): ResultActions =
            mockMvc.perform(
                post("$BASE_TRIP_REPORT_URL/{tripReportId}/images/presigned", tripReportId)
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
            val resultActions = getResultActions("", tripReport.id, request)

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
            val resultActions = getResultActions(token, tripReport.id, request)

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
            val resultActions = getResultActions(token, tripReport.id, request)

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
            val resultActions = getResultActions(token, tripReport.id, request)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.presignedUrl").isNotEmpty)
                .andExpect(jsonPath("$.data.tmpKey").isNotEmpty)
                .andExpect(jsonPath("$.data.tmpKey").value(Matchers.startsWith("tmp/trip-reports/")))
                .andExpect(jsonPath("$.data.tmpKey").value(Matchers.containsString(tripReport.id.toString())))

            // S3Provider 호출 검증
            verify(s3ImageStorageProvider).issuePresignedUrl(anyString())
        }
    }

    @Nested
    @DisplayName("여행 리포트 이미지 확정 API")
    inner class ConfirmImage {
        private val fixture = ConfirmTripReportImageRequestFixture()

        private fun getResultActions(
            token: String,
            tripReportId: Any,
            request: ConfirmTripReportImageRequest,
        ): ResultActions =
            mockMvc.perform(
                post("$BASE_TRIP_REPORT_URL/{tripReportId}/images/confirm", tripReportId)
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
            val resultActions = getResultActions("", tripReport.id, request)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("PathVariable 여행 리포트 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenStudyLogIdTypeMismatch() {
            // given
            val tripReportId = "abc"
            val request = fixture.build()

            // when
            val resultActions = getResultActions(token, tripReportId, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("tmpKey가 비어있으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTmpKeyIsEmpty() {
            // given
            val request = fixture.withTmpKey("").build()

            // when
            val resultActions = getResultActions(token, tripReport.id, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.message))
        }
    }

    @Nested
    @DisplayName("여행 회고 API")
    inner class LoadTripRetrospect {
        private fun getResultActions(
            token: String,
            tripId: Any,
            page: String,
            size: String,
        ): ResultActions =
            mockMvc.perform(
                get("/api/trips/{tripId}/retrospect", tripId)
                    .param("page", page)
                    .param("size", size)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture.authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", completedTrip.id, DEFAULT_PAGE, DEFAULT_SIZE)

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
            val resultActions = getResultActions(token, completedTrip.id, page, size)

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
            val resultActions = getResultActions(token, completedTrip.id, page, size)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.CONSTRAINT_VIOLATION.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.CONSTRAINT_VIOLATION.message))
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripIdTypeMismatch() {
            // given
            val tripId = "abc"

            // when
            val resultActions = getResultActions(token, tripId, DEFAULT_PAGE, DEFAULT_SIZE)

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
            val resultActions = getResultActions(token, tripId, DEFAULT_PAGE, DEFAULT_SIZE)

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
            val newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE)

            // when
            val resultActions = getResultActions(token, newTrip.id, DEFAULT_PAGE, DEFAULT_SIZE)

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
            val resultActions = getResultActions(token, deletedTrip.id, DEFAULT_PAGE, DEFAULT_SIZE)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("여행이 아직 완료되지 않았다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripDoesNotCompleted() {
            // given
            val trip = tripTestHelper.saveTrip(member, TripCategory.COURSE)

            // when
            val resultActions = getResultActions(token, trip.id, DEFAULT_PAGE, DEFAULT_SIZE)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripErrorCode.TRIP_NOT_COMPLETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripErrorCode.TRIP_NOT_COMPLETED.message))
        }

        @Test
        @DisplayName("유효한 여행 ID가 들어오면 여행 회고 정보를 반환한다.")
        fun shouldReturnTripRetrospectWhenTripIdIsValid() {
            // when
            val resultActions = getResultActions(token, completedTrip.id, DEFAULT_PAGE, DEFAULT_SIZE)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").isNotEmpty)
        }
    }

    @Nested
    @DisplayName("여행 리포트 목록 조회 API")
    inner class LoadTripReports {
        private fun getResultActions(token: String): ResultActions =
            mockMvc.perform(
                get(BASE_TRIP_REPORT_URL)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture.authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("")

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("특정 여행 리포트 목록을 조회하고 반환한다.")
        fun shouldReturnTripReports() {
            // when
            val resultActions = getResultActions(token)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").isNotEmpty)
        }
    }

    @Nested
    @DisplayName("여행 리포트 상세 조회 API")
    inner class LoadTripReport {
        private fun getResultActions(
            token: String,
            tripReportId: Any,
            page: String,
            size: String,
        ): ResultActions =
            mockMvc.perform(
                get("$BASE_TRIP_REPORT_URL/{tripReportId}", tripReportId)
                    .param("page", page)
                    .param("size", size)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture.authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("", tripReport.id, DEFAULT_PAGE, DEFAULT_SIZE)

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
            val resultActions = getResultActions(token, tripReport.id, page, size)

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
            val resultActions = getResultActions(token, tripReport.id, page, size)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.CONSTRAINT_VIOLATION.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.CONSTRAINT_VIOLATION.message))
        }

        @Test
        @DisplayName("PathVariable 여행 리포트 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripReportIdTypeMismatch() {
            // given
            val tripReportId = "abc"

            // when
            val resultActions = getResultActions(token, tripReportId, DEFAULT_PAGE, DEFAULT_SIZE)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("여행 리포트가 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenTripReportDoesNotExist() {
            // given
            val tripReportId = -1L

            // when
            val resultActions = getResultActions(token, tripReportId, DEFAULT_PAGE, DEFAULT_SIZE)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripReportErrorCode.TRIP_REPORT_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripReportErrorCode.TRIP_REPORT_NOT_FOUND.message))
        }

        @Test
        @DisplayName("여행 리포트의 소유자가 아니라면 403 Forbidden을 반환한다.")
        fun shouldReturnForbiddenWhenMemberIsNotTripReportOwner() {
            // given
            val newTripReport = tripReportTestHelper.saveTripReport(newMember)

            // when
            val resultActions = getResultActions(token, newTripReport.id, DEFAULT_PAGE, DEFAULT_SIZE)

            // then
            resultActions
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripReportErrorCode.NOT_TRIP_REPORT_OWNER.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripReportErrorCode.NOT_TRIP_REPORT_OWNER.message))
        }

        @Test
        @DisplayName("여행 리포트가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTripReportAlreadyDeleted() {
            // given
            val deletedTripReport = tripReportTestHelper.saveDeletedTripReport(member)

            // when
            val resultActions = getResultActions(token, deletedTripReport.id, DEFAULT_PAGE, DEFAULT_SIZE)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(TripReportErrorCode.TRIP_REPORT_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(TripReportErrorCode.TRIP_REPORT_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("특정 여행 리포트를 상세 조회하고 반환한다.")
        fun shouldReturnTripReport() {
            // when
            val resultActions = getResultActions(token, tripReport.id, DEFAULT_PAGE, DEFAULT_SIZE)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").isNotEmpty)
        }
    }
}
