package com.ject.studytrip.trip.presentation.controller;

import static com.ject.studytrip.auth.fixture.TokenFixture.TOKEN_PREFIX;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ject.studytrip.BaseIntegrationTest;
import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.fixture.TokenFixture;
import com.ject.studytrip.auth.helper.TokenTestHelper;
import com.ject.studytrip.global.exception.error.CommonErrorCode;
import com.ject.studytrip.image.domain.error.ImageErrorCode;
import com.ject.studytrip.image.infra.s3.provider.S3ImageStorageProvider;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.helper.MemberTestHelper;
import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.helper.DailyMissionTestHelper;
import com.ject.studytrip.mission.helper.MissionTestHelper;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.helper.StampTestHelper;
import com.ject.studytrip.studylog.domain.error.StudyLogErrorCode;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.helper.StudyLogDailyMissionTestHelper;
import com.ject.studytrip.studylog.helper.StudyLogTestHelper;
import com.ject.studytrip.trip.domain.error.TripErrorCode;
import com.ject.studytrip.trip.domain.error.TripReportErrorCode;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.domain.model.TripReport;
import com.ject.studytrip.trip.fixture.ConfirmTripReportImageRequestFixture;
import com.ject.studytrip.trip.fixture.CreateTripReportRequestFixture;
import com.ject.studytrip.trip.fixture.PresignTripReportImageRequestFixture;
import com.ject.studytrip.trip.helper.DailyGoalTestHelper;
import com.ject.studytrip.trip.helper.TripReportTestHelper;
import com.ject.studytrip.trip.helper.TripTestHelper;
import com.ject.studytrip.trip.presentation.dto.request.ConfirmTripReportImageRequest;
import com.ject.studytrip.trip.presentation.dto.request.CreateTripReportRequest;
import com.ject.studytrip.trip.presentation.dto.request.PresignTripReportImageRequest;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("TripReportController 통합 테스트")
class TripReportControllerIntegrationTest extends BaseIntegrationTest {
    @Autowired private MemberTestHelper memberTestHelper;
    @Autowired private TokenTestHelper tokenTestHelper;
    @Autowired private TripTestHelper tripTestHelper;
    @Autowired private StampTestHelper stampTestHelper;
    @Autowired private MissionTestHelper missionTestHelper;
    @Autowired private DailyGoalTestHelper dailyGoalTestHelper;
    @Autowired private StudyLogTestHelper studyLogTestHelper;
    @Autowired private DailyMissionTestHelper dailyMissionTestHelper;
    @Autowired private StudyLogDailyMissionTestHelper studyLogDailyMissionTestHelper;
    @Autowired private TripReportTestHelper tripReportTestHelper;

    @MockitoBean S3ImageStorageProvider s3ImageStorageProvider;

    private String accessToken;
    private String newAccessToken;

    private Member member;
    private Trip courseTrip;

    private TripReport tripReport;
    private StudyLog studyLog1;
    private StudyLog studyLog2;

    @BeforeEach
    void setUp() {
        member = memberTestHelper.saveMember();
        Member newMember = memberTestHelper.saveMember("test@kakao.com", "TEST NICKNAME");

        accessToken =
                tokenTestHelper.createAccessToken(
                        member.getId().toString(), member.getRole().name());
        newAccessToken =
                tokenTestHelper.createAccessToken(
                        newMember.getId().toString(), newMember.getRole().name());

        courseTrip = tripTestHelper.saveCompletedTrip(member, TripCategory.COURSE);
        Stamp stamp = stampTestHelper.saveStamp(courseTrip, 1);
        DailyGoal dailyGoal = dailyGoalTestHelper.saveDailyGoal(courseTrip);
        Mission mission = missionTestHelper.saveMission(stamp);
        DailyMission dailyMission = dailyMissionTestHelper.saveDailyMission(mission, dailyGoal);
        tripReport = tripReportTestHelper.saveTripReport(member);
        studyLog1 = studyLogTestHelper.saveStudyLog(member, dailyGoal);
        studyLog2 = studyLogTestHelper.saveStudyLog(member, dailyGoal);
        studyLogDailyMissionTestHelper.saveStudyLogDailyMissions(studyLog2, dailyMission);
    }

    @Nested
    @DisplayName("여행 회고 API")
    class LoadTripRetrospect {
        private static final String DEFAULT_PAGE = "0";
        private static final String DEFAULT_PAGE_SIZE = "5";

        private ResultActions getResultActions(
                String accessToken, Object tripId, String page, String size) throws Exception {
            return mockMvc.perform(
                    get("/api/trips/{tripId}/retrospect", tripId)
                            .param("page", page)
                            .param("size", size)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + accessToken));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions("", courseTrip.getId(), DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("Request Param 페이징 데이터 타입이 올바르지 않으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenWhenPagingParameterTypeMismatch() throws Exception {
            // Given
            String page = "test";
            String size = "test";

            // when
            ResultActions resultActions = getResultActions(accessToken, courseTrip, page, size);

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
        @DisplayName("Request Param 페이징 데이터가 유효하지 않으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenWhenPagingParameterIsInvalid() throws Exception {
            // Given
            String page = "-1";
            String size = "100";

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), page, size);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            CommonErrorCode.METHOD_ARGUMENT_NOT_VALID
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTripIdTypeMismatch() throws Exception {
            // given
            String invalidTripId = "abc";

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, invalidTripId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

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
        @DisplayName("삭제된 여행일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTripAlreadyDeleted() throws Exception {
            // given
            courseTrip.updateDeletedAt();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken, courseTrip.getId(), DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_ALREADY_DELETED.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(TripErrorCode.TRIP_ALREADY_DELETED.getMessage()));
        }

        @Test
        @DisplayName("아직 완료되지 않은 여행일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTripDoesNotCompleted() throws Exception {
            // given
            Trip courseTrip2 = tripTestHelper.saveTrip(member, TripCategory.COURSE);

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken, courseTrip2.getId(), DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_NOT_COMPLETED.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(TripErrorCode.TRIP_NOT_COMPLETED.getMessage()));
        }

        @Test
        @DisplayName("여행의 소유자가 아니라면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenNotTripOwner() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(
                            newAccessToken, courseTrip.getId(), DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(TripErrorCode.NOT_TRIP_OWNER.getMessage()));
        }

        @Test
        @DisplayName("유효하지 않은 여행 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenTripIdIsInvalid() throws Exception {
            // given
            Long invalidTripId = 10000L;

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, invalidTripId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(TripErrorCode.TRIP_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("유효한 여행 ID가 들어오면 여행 회고 정보를 반환한다.")
        void shouldReturnTripRetrospectWhenTripIdIsValid() throws Exception {
            // when
            ResultActions result = getResultActions(accessToken, courseTrip.getId(), "1", "10");

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data.name").isString())
                    .andExpect(jsonPath("$.data.totalFocusHours").isNumber())
                    .andExpect(jsonPath("$.data.studyLogCount").isNumber())
                    .andExpect(jsonPath("$.data.studyDays").isNumber())
                    .andExpect(jsonPath("$.data.history").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("여행 리포트 목록 조회 API")
    class LoadTripReports {
        private ResultActions getResultActions(String accessToken, Object tripId) throws Exception {
            return mockMvc.perform(
                    get("/api/trip-reports", tripId)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + accessToken));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // when
            ResultActions resultActions = getResultActions("", courseTrip.getId());

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("유효한 멤버 ID가 들어오면 여행 리포트 목록을 반환한다.")
        void shouldReturnLoadTripReportsWhenMemberIdIsValid() throws Exception {
            // given
            Long memberId = member.getId();

            // when
            ResultActions resultActions = getResultActions(accessToken, memberId);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.summary").exists())
                    .andExpect(jsonPath("$.data.tripReports").isArray());
        }
    }

    @Nested
    @DisplayName("여행 리포트 상세 조회 API")
    class LoadTripReport {
        private static final String DEFAULT_PAGE = "0";
        private static final String DEFAULT_PAGE_SIZE = "5";

        private ResultActions getResultActions(
                String accessToken, Object tripReportId, String page, String size)
                throws Exception {
            return mockMvc.perform(
                    get("/api/trip-reports/{tripReportId}", tripReportId)
                            .param("page", page)
                            .param("size", size)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + accessToken));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions("", courseTrip.getId(), DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("Request Param 페이징 데이터 타입이 올바르지 않으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenWhenPagingParameterTypeMismatch() throws Exception {
            // Given
            String page = "test";
            String size = "test";

            // when
            ResultActions resultActions = getResultActions(accessToken, courseTrip, page, size);

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
        @DisplayName("Request Param 페이징 데이터가 유효하지 않으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenWhenPagingParameterIsInvalid() throws Exception {
            // Given
            String page = "-1";
            String size = "100";

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), page, size);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            CommonErrorCode.METHOD_ARGUMENT_NOT_VALID
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("PathVariable 여행 리포트 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTripReportIdTypeMismatch() throws Exception {
            // given
            String invalidId = "abc";

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, invalidId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

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
        @DisplayName("여행 리포트의 소유자가 아니라면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenNotTripReportOwner() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(
                            newAccessToken, tripReport.getId(), DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            TripReportErrorCode.NOT_TRIP_REPORT_OWNER
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(TripReportErrorCode.NOT_TRIP_REPORT_OWNER.getMessage()));
        }

        @Test
        @DisplayName("유효하지 않은 여행 리포트 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenTripReportIdIsInvalid() throws Exception {
            // given
            Long invalidId = -1L;

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, invalidId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            TripReportErrorCode.TRIP_REPORT_NOT_FOUND
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(TripReportErrorCode.TRIP_REPORT_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("유효한 여행 리포트 ID가 들어오면 여행 리포트를 반환한다.")
        void shouldReturnTripReportWhenTripReportIdIsValid() throws Exception {
            // given
            Long tripReportId = tripReport.getId();

            // when
            ResultActions resultActions = getResultActions(accessToken, tripReportId, "1", "10");

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.tripReportId").value(tripReportId))
                    .andExpect(jsonPath("$.data.title").value(tripReport.getTitle()))
                    .andExpect(jsonPath("$.data.content").value(tripReport.getContent()))
                    .andExpect(jsonPath("$.data.startDate").value(tripReport.getStartDate()))
                    .andExpect(jsonPath("$.data.endDate").value(tripReport.getEndDate()))
                    .andExpect(
                            jsonPath("$.data.totalFocusHours")
                                    .value(tripReport.getTotalFocusHours()))
                    .andExpect(
                            jsonPath("$.data.studyLogCount").value(tripReport.getStudyLogCount()))
                    .andExpect(jsonPath("$.data.studyDays").value(tripReport.getStudyDays()))
                    .andExpect(jsonPath("$.data.imageTitle").value(tripReport.getImageTitle()))
                    .andExpect(jsonPath("$.data.imageUrl").value(tripReport.getImageUrl()))
                    .andExpect(jsonPath("$.data.history").exists())
                    .andExpect(jsonPath("$.data.history.studyLogs").isArray())
                    .andExpect(jsonPath("$.data.history.hasNext").isBoolean());
        }
    }

    @Nested
    @DisplayName("여행 리포트 생성 API")
    class CreateTripReport {
        private final CreateTripReportRequestFixture fixture = new CreateTripReportRequestFixture();

        private ResultActions getResultActions(String accessToken, CreateTripReportRequest request)
                throws Exception {
            return mockMvc.perform(
                    post("/api/trip-reports")
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
            CreateTripReportRequest request = fixture.build();

            // when
            ResultActions resultActions = getResultActions("", request);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("요청한 학습 로그 ID 목록으로 해당 학습 로그를 조회하고, 하나라도 일치하지 않는 경우 404 NotFound를 반환한다.")
        void shouldReturnNotFoundWhenAnyStudyLogIdDoesNotExist() throws Exception {
            // given
            CreateTripReportRequest request =
                    fixture.withStudyLogIds(List.of(studyLog1.getId(), -1L)).build();

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StudyLogErrorCode.STUDY_LOG_NOT_FOUND
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(StudyLogErrorCode.STUDY_LOG_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("조회된 학습 로그 목록 중 삭제된 학습 로그가 존재하면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenStudyLogAlreadyDeleted() throws Exception {
            // given
            studyLog1.updateDeletedAt();
            List<Long> studyLogIds = List.of(studyLog1.getId(), studyLog2.getId());
            CreateTripReportRequest request = fixture.withStudyLogIds(studyLogIds).build();

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StudyLogErrorCode.STUDY_LOG_ALREADY_DELETED
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(
                                            StudyLogErrorCode.STUDY_LOG_ALREADY_DELETED
                                                    .getMessage()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 여행 리포트를 생성하고 반환한다.")
        void shouldCreateTripReportWhenRequestIsValid() throws Exception {
            // given
            List<Long> studyLogIds = List.of(studyLog1.getId(), studyLog2.getId());
            CreateTripReportRequest request = fixture.withStudyLogIds(studyLogIds).build();

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.tripReportId").isNumber());
        }
    }

    @Nested
    @DisplayName("여행 리포트 이미지 Presigned URL 발급 API")
    class IssuePresignedUrl {
        private static final String PRESIGNED_URL = "/api/trip-reports/%d/images/presigned";

        private final PresignTripReportImageRequestFixture fixture =
                new PresignTripReportImageRequestFixture();

        private ResultActions getResultActions(
                String accessToken, Long tripReportId, PresignTripReportImageRequest request)
                throws Exception {
            return mockMvc.perform(
                    post(String.format(PRESIGNED_URL, tripReportId))
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
            PresignTripReportImageRequest request = fixture.build();

            // when
            ResultActions resultActions = getResultActions("", tripReport.getId(), request);

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
        @DisplayName("파일명이 비어있으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenFilenameIsEmpty() throws Exception {
            // given
            PresignTripReportImageRequest request = fixture.withOriginFilename("").build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, tripReport.getId(), request);

            // then
            resultActions.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("유효하지 않은 확장자는 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenExtensionIsInvalid() throws Exception {
            // given
            PresignTripReportImageRequest request = fixture.withOriginFilename("test.pdf").build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, tripReport.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            ImageErrorCode.INVALID_IMAGE_EXTENSION
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(ImageErrorCode.INVALID_IMAGE_EXTENSION.getMessage()));
        }

        @Test
        @DisplayName("유효한 파일명으로 Presigned URL을 발급한다.")
        void shouldIssuePresignedUrlWhenFilenameIsValid() throws Exception {
            // given
            PresignTripReportImageRequest request = fixture.build();
            given(s3ImageStorageProvider.issuePresignedUrl(anyString()))
                    .willReturn("https://mocked-presigned-url.com");

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, tripReport.getId(), request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.data.presignedUrl").isNotEmpty())
                    .andExpect(jsonPath("$.data.tmpKey").isNotEmpty())
                    .andExpect(
                            jsonPath("$.data.tmpKey")
                                    .value(Matchers.startsWith("tmp/trip-reports/")))
                    .andExpect(
                            jsonPath("$.data.tmpKey")
                                    .value(Matchers.containsString(tripReport.getId().toString())));

            verify(s3ImageStorageProvider).issuePresignedUrl(anyString());
        }
    }

    @Nested
    @DisplayName("여행 리포트 이미지 확정 API")
    class ConfirmImage {
        private static final String CONFIRM_URL = "/api/trip-reports/%d/images/confirm";

        private final ConfirmTripReportImageRequestFixture fixture =
                new ConfirmTripReportImageRequestFixture();

        private ResultActions getResultActions(
                String accessToken, Long tripReportId, ConfirmTripReportImageRequest request)
                throws Exception {
            return mockMvc.perform(
                    post(String.format(CONFIRM_URL, tripReportId))
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
            ConfirmTripReportImageRequest request = fixture.build();

            // when
            ResultActions resultActions = getResultActions("", tripReport.getId(), request);

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
        @DisplayName("tmpKey가 비어있으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTmpKeyIsEmpty() throws Exception {
            // given
            ConfirmTripReportImageRequest request = fixture.withTmpKey("").build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, tripReport.getId(), request);

            // then
            resultActions.andExpect(status().isBadRequest());
        }
    }
}
