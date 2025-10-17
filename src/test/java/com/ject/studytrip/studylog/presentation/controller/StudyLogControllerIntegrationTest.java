package com.ject.studytrip.studylog.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.ject.studytrip.member.domain.model.MemberRole;
import com.ject.studytrip.member.helper.MemberTestHelper;
import com.ject.studytrip.mission.domain.error.DailyMissionErrorCode;
import com.ject.studytrip.mission.domain.error.MissionErrorCode;
import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.helper.DailyMissionTestHelper;
import com.ject.studytrip.mission.helper.MissionTestHelper;
import com.ject.studytrip.pomodoro.domain.error.PomodoroErrorCode;
import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import com.ject.studytrip.pomodoro.helper.PomodoroTestHelper;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.helper.StampTestHelper;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import com.ject.studytrip.studylog.fixture.CreateStudyLogRequestFixture;
import com.ject.studytrip.studylog.helper.StudyLogDailyMissionTestHelper;
import com.ject.studytrip.studylog.helper.StudyLogTestHelper;
import com.ject.studytrip.studylog.presentation.dto.request.ConfirmStudyLogImageRequest;
import com.ject.studytrip.studylog.presentation.dto.request.CreateStudyLogRequest;
import com.ject.studytrip.studylog.presentation.dto.request.PresignStudyLogImageRequest;
import com.ject.studytrip.trip.domain.error.DailyGoalErrorCode;
import com.ject.studytrip.trip.domain.error.TripErrorCode;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.helper.DailyGoalTestHelper;
import com.ject.studytrip.trip.helper.TripTestHelper;
import java.util.List;
import org.apache.http.HttpHeaders;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("StudyLogController 통합 테스트")
public class StudyLogControllerIntegrationTest extends BaseIntegrationTest {
    @Autowired private MemberTestHelper memberTestHelper;
    @Autowired private TokenTestHelper tokenTestHelper;
    @Autowired private TripTestHelper tripTestHelper;
    @Autowired private StampTestHelper stampTestHelper;
    @Autowired private MissionTestHelper missionTestHelper;
    @Autowired private DailyGoalTestHelper dailyGoalTestHelper;
    @Autowired private DailyMissionTestHelper dailyMissionTestHelper;
    @Autowired private StudyLogTestHelper studyLogTestHelper;
    @Autowired private StudyLogDailyMissionTestHelper studyLogDailyMissionTestHelper;
    @Autowired private PomodoroTestHelper pomodoroTestHelper;

    @MockitoBean S3ImageStorageProvider s3ImageStorageProvider;

    private Member member;
    private String token;
    private Trip courseTrip;
    private Stamp stamp;
    private Mission mission;
    private DailyGoal dailyGoal;
    private DailyMission dailyMission;
    private Pomodoro pomodoro;

    @BeforeEach
    void setUp() {
        member = memberTestHelper.saveMember();
        token =
                tokenTestHelper.createAccessToken(
                        member.getId().toString(), MemberRole.ROLE_USER.name());
        courseTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE);
        stamp = stampTestHelper.saveStamp(courseTrip, 1);
        mission = missionTestHelper.saveMission(stamp);
        dailyGoal = dailyGoalTestHelper.saveDailyGoal(courseTrip);
        dailyMission = dailyMissionTestHelper.saveDailyMission(mission, dailyGoal);
        pomodoro = pomodoroTestHelper.savePomodoro(dailyGoal);
    }

    @Nested
    @DisplayName("학습 로그 생성 API")
    class CreateStudyLog {
        private final CreateStudyLogRequestFixture fixture = new CreateStudyLogRequestFixture();

        private ResultActions getResultActions(
                String token, Object tripId, Object dailyGoalId, CreateStudyLogRequest request)
                throws Exception {
            return mockMvc.perform(
                    post(
                                    "/api/trips/{tripId}/daily-goals/{dailyGoalId}/study-logs",
                                    tripId,
                                    dailyGoalId)
                            .header(HttpHeaders.AUTHORIZATION, TokenFixture.TOKEN_PREFIX + token)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("유효한 요청으로 학습 로그를 생성한다")
        void shouldCreateStudyLog() throws Exception {
            // given
            CreateStudyLogRequest request =
                    fixture.withSelectedDailyMissionIds(List.of(dailyMission.getId())).build();
            int initialCompletedMissions = stamp.getCompletedMissions();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), dailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.studyLogId").isNumber());

            // 스탬프의 완료된 미션 수가 증가했는지 확인
            assertThat(stamp.getCompletedMissions()).isEqualTo(initialCompletedMissions + 1);
        }

        @Test
        @DisplayName("여러 미션을 선택한 경우 스탬프의 완료된 미션 수가 올바르게 증가한다")
        void shouldUpdateCompletedMissionsWhenMultipleMissionsSelected() throws Exception {
            // given
            Mission mission2 = missionTestHelper.saveMission(stamp);
            DailyMission dailyMission2 =
                    dailyMissionTestHelper.saveDailyMission(mission2, dailyGoal);
            CreateStudyLogRequest request =
                    fixture.withSelectedDailyMissionIds(
                                    List.of(dailyMission.getId(), dailyMission2.getId()))
                            .build();
            int initialCompletedMissions = stamp.getCompletedMissions();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), dailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.studyLogId").isNumber());

            // 스탬프의 완료된 미션 수가 2개 증가했는지 확인
            assertThat(stamp.getCompletedMissions()).isEqualTo(initialCompletedMissions + 2);
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 Unauthorized를 반환한다")
        void shouldReturnUnauthorizedWhenUnauthenticated() throws Exception {
            // given
            CreateStudyLogRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions("", courseTrip.getId(), dailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenTripIdTypeMismatch() throws Exception {
            // given
            String tripId = "abc";
            CreateStudyLogRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(token, tripId, dailyGoal.getId(), request);

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
        @DisplayName("PathVariable 데일리 목표 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenDailyGoalIdTypeMismatch() throws Exception {
            // given
            String dailyGoalId = "abc";
            CreateStudyLogRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), dailyGoalId, request);

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
        @DisplayName("학습 로그를 생성하는데 필요한 필수 요청 값이 누락되거나 유효하지 않으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenInvalidRequiredFields() throws Exception {
            // given
            CreateStudyLogRequest request =
                    fixture.withTotalFocusTimeInMinutes(-30)
                            .withSelectedDailyMissionIds(List.of())
                            .build();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), dailyGoal.getId(), request);

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
        @DisplayName("유효하지 않은 여행 ID 라면 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenInvalidTripId() throws Exception {
            // given
            Long tripId = 10000L;
            CreateStudyLogRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(token, tripId, dailyGoal.getId(), request);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("요청한 여행의 소유자가 아닐 경우 403 Forbidden을 반환한다")
        void shouldReturnForbiddenWhenNotTripOwner() throws Exception {
            // given
            Member newMember = memberTestHelper.saveMember("test@gmail.com", "TEST");
            Trip newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE);
            CreateStudyLogRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(token, newTrip.getId(), dailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
        }

        @Test
        @DisplayName("삭제된 여행일 경우 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenAlreadyTrip() throws Exception {
            // given
            Trip deleted = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE);
            CreateStudyLogRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(token, deleted.getId(), dailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_ALREADY_DELETED.getStatus().value()));
        }

        @Test
        @DisplayName("유효하지 않은 데일리 목표 ID 라면 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenInvalidDailyGoalId() throws Exception {
            // given
            Long dailyGoalId = 10000L;
            CreateStudyLogRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), dailyGoalId, request);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("조회된 데일리 목표가 요청한 여행에 속하지 않을 경우 403 Forbidden을 반환한다")
        void shouldReturnForbiddenWhenDailyGoalNotBelongToTrip() throws Exception {
            // given
            Trip newTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE);
            DailyGoal newDailyGoal = dailyGoalTestHelper.saveDailyGoal(newTrip);
            CreateStudyLogRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), newDailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            DailyGoalErrorCode.DAILY_GOAL_NOT_BELONG_TO_TRIP
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("삭제된 데일리 목표일 경우 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenAlreadyDailyGoal() throws Exception {
            // given
            DailyGoal deleted = dailyGoalTestHelper.saveDeletedDailyGoal(courseTrip);
            CreateStudyLogRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), deleted.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("선택한 데일리 미션 목록과 조회된 데일리 미션 목록이 일치하지 않으면 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenAlreadyDailyMissions() throws Exception {
            // given
            CreateStudyLogRequest request =
                    fixture.withSelectedDailyMissionIds(List.of(dailyMission.getId(), 1000L))
                            .build();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), dailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            DailyMissionErrorCode.DAILY_MISSION_NOT_FOUND
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("조회된 데일리 미션이 요청한 데일리 목표에 속하지 않을 경우 403 Forbidden을 반환한다")
        void shouldReturnForbiddenWhenDailyMissionNotBelongToDailyGoal() throws Exception {
            // given
            DailyGoal newDailyGoal = dailyGoalTestHelper.saveDailyGoal(courseTrip);
            CreateStudyLogRequest request =
                    fixture.withSelectedDailyMissionIds(List.of(dailyMission.getId())).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), newDailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            DailyMissionErrorCode
                                                    .DAILY_MISSION_NOT_BELONG_TO_DAILY_GOAL
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("삭제된 데일리 미션일 경우 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenAlreadyDailyMission() throws Exception {
            // given
            DailyMission deleted =
                    dailyMissionTestHelper.saveDeletedDailyMission(mission, dailyGoal);
            CreateStudyLogRequest request =
                    fixture.withSelectedDailyMissionIds(List.of(deleted.getId())).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), dailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            DailyMissionErrorCode.DAILY_MISSION_ALREADY_DELETED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("요청한 DailyGoalId의 뽀모도로가 존재하지 않을 경우 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenPomodoroNotFound() throws Exception {
            // given
            DailyGoal newDailyGoal = dailyGoalTestHelper.saveDailyGoal(courseTrip);
            CreateStudyLogRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), newDailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            PomodoroErrorCode.POMODORO_NOT_FOUND
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("요청한 DailyGoalId의 뽀모도로가 삭제된 경우 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenPomodoroIsDeleted() throws Exception {
            // given
            DailyGoal newDailyGoal = dailyGoalTestHelper.saveDailyGoal(courseTrip);
            DailyMission newDailyMission =
                    dailyMissionTestHelper.saveDailyMission(mission, newDailyGoal);
            pomodoroTestHelper.saveDeletedPomodoro(newDailyGoal);

            CreateStudyLogRequest request =
                    fixture.withSelectedDailyMissionIds(List.of(newDailyMission.getId())).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), newDailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            PomodoroErrorCode.POMODORO_ALREADY_DELETED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("선택된 미션들을 완료 처리할 때, 이미 삭제된 미션일 경우 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenSelectedMissionIsAlreadyDeleted() throws Exception {
            // given
            Mission deletedMission = missionTestHelper.saveDeletedMission(stamp);
            DailyMission newDailyMission =
                    dailyMissionTestHelper.saveDailyMission(deletedMission, dailyGoal);
            CreateStudyLogRequest request =
                    fixture.withSelectedDailyMissionIds(List.of(newDailyMission.getId())).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), dailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            MissionErrorCode.MISSION_ALREADY_DELETED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("선택된 미션들을 완료 처리할 때, 이미 완료된 미션일 경우 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenSelectedMissionIsAlreadyCompleted() throws Exception {
            // given
            Mission deletedMission = missionTestHelper.saveCompletedMission(stamp);
            DailyMission newDailyMission =
                    dailyMissionTestHelper.saveDailyMission(deletedMission, dailyGoal);
            CreateStudyLogRequest request =
                    fixture.withSelectedDailyMissionIds(List.of(newDailyMission.getId())).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), dailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            MissionErrorCode.MISSION_ALREADY_COMPLETED
                                                    .getStatus()
                                                    .value()));
        }
    }

    @Nested
    @DisplayName("학습 로그 목록 조회 API")
    class ListStudyLogs {
        private static final String DEFAULT_PAGE = "0";
        private static final String DEFAULT_PAGE_SIZE = "5";
        private static final String DEFAULT_ORDER = "LATEST";

        private ResultActions getResultActions(
                String token, Object tripId, String page, String size, String order)
                throws Exception {
            return mockMvc.perform(
                    get("/api/trips/{tripId}/study-logs", tripId)
                            .param("page", page)
                            .param("size", size)
                            .param("order", order)
                            .header(HttpHeaders.AUTHORIZATION, TokenFixture.TOKEN_PREFIX + token));
        }

        @Test
        @DisplayName("특정 여행의 학습 로그 목록을 조회하고 슬라이스 처리해 반환한다")
        void shouldLoadStudyLogsByTripWithSlicePaging() throws Exception {
            // given
            StudyLog studyLog = studyLogTestHelper.saveStudyLog(member, dailyGoal);
            List<StudyLogDailyMission> studyLogDailyMissions =
                    studyLogDailyMissionTestHelper.saveStudyLogDailyMissions(
                            studyLog, dailyMission);

            // when
            ResultActions resultActions =
                    getResultActions(
                            token,
                            courseTrip.getId(),
                            DEFAULT_PAGE,
                            DEFAULT_PAGE_SIZE,
                            DEFAULT_ORDER);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.studyLogs").isNotEmpty())
                    .andExpect(jsonPath("$.data.hasNext").value(false))
                    .andExpect(jsonPath("$.data.studyLogs[0].studyLogId").value(studyLog.getId()))
                    .andExpect(jsonPath("$.data.studyLogs[0].dailyMissions").isNotEmpty())
                    .andExpect(
                            jsonPath("$.data.studyLogs[0].dailyMissions")
                                    .value(Matchers.hasSize(studyLogDailyMissions.size())));
        }

        @Test
        @DisplayName("order 파라미터를 OLDEST로 지정하면 과거순으로 정렬된 학습 로그 목록을 반환한다")
        void shouldLoadStudyLogsByTripWithOldestOrder() throws Exception {
            // given
            StudyLog studyLog = studyLogTestHelper.saveStudyLog(member, dailyGoal);
            List<StudyLogDailyMission> studyLogDailyMissions =
                    studyLogDailyMissionTestHelper.saveStudyLogDailyMissions(
                            studyLog, dailyMission);

            // when
            ResultActions resultActions =
                    getResultActions(
                            token, courseTrip.getId(), DEFAULT_PAGE, DEFAULT_PAGE_SIZE, "OLDEST");

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.studyLogs").isNotEmpty())
                    .andExpect(jsonPath("$.data.hasNext").value(false))
                    .andExpect(jsonPath("$.data.studyLogs[0].studyLogId").value(studyLog.getId()))
                    .andExpect(jsonPath("$.data.studyLogs[0].dailyMissions").isNotEmpty())
                    .andExpect(
                            jsonPath("$.data.studyLogs[0].dailyMissions")
                                    .value(Matchers.hasSize(studyLogDailyMissions.size())));
        }

        @Test
        @DisplayName("order 파라미터가 유효하지 않은 값이면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenOrderIsInvalid() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(
                            token, courseTrip.getId(), DEFAULT_PAGE, DEFAULT_PAGE_SIZE, "INVALID");

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
        @DisplayName("인증되지 않은 사용자일 경우 401 Unauthorized를 반환한다")
        void shouldReturnUnauthorizedWhenUnauthenticated() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(
                            "", courseTrip.getId(), DEFAULT_PAGE, DEFAULT_PAGE_SIZE, DEFAULT_ORDER);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenTripIdTypeMismatch() throws Exception {
            // given
            String tripId = "abc";

            // when
            ResultActions resultActions =
                    getResultActions(token, tripId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, DEFAULT_ORDER);

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
        @DisplayName("Request Param 페이징 데이터 타입이 올바르지 않으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenWhenPagingParameterTypeMismatch() throws Exception {
            // Given
            String page = "test";
            String size = "test";

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip, page, size, DEFAULT_ORDER);

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
                    getResultActions(token, courseTrip.getId(), page, size, DEFAULT_ORDER);

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
        @DisplayName("유효하지 않은 여행 ID 라면 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenInvalidTripId() throws Exception {
            // given
            Long tripId = 10000L;

            // when
            ResultActions resultActions =
                    getResultActions(token, tripId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, DEFAULT_ORDER);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("요청한 여행의 소유자가 아닐 경우 403 Forbidden을 반환한다")
        void shouldReturnForbiddenWhenNotTripOwner() throws Exception {
            // given
            Member newMember = memberTestHelper.saveMember("test@gmail.com", "TEST");
            Trip newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE);

            // when
            ResultActions resultActions =
                    getResultActions(
                            token, newTrip.getId(), DEFAULT_PAGE, DEFAULT_PAGE_SIZE, DEFAULT_ORDER);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
        }

        @Test
        @DisplayName("삭제된 여행일 경우 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenAlreadyTrip() throws Exception {
            // given
            Trip deleted = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE);

            // when
            ResultActions resultActions =
                    getResultActions(
                            token, deleted.getId(), DEFAULT_PAGE, DEFAULT_PAGE_SIZE, DEFAULT_ORDER);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_ALREADY_DELETED.getStatus().value()));
        }
    }

    @Nested
    @DisplayName("학습 로그 이미지 Presigned URL 발급 API")
    class IssuePresignedUrl {
        private static final String PRESIGNED_URL = "/api/study-logs/%d/images/presigned";

        private ResultActions getResultActions(
                String token, Long studyLogId, PresignStudyLogImageRequest request)
                throws Exception {
            return mockMvc.perform(
                    post(String.format(PRESIGNED_URL, studyLogId))
                            .header(HttpHeaders.AUTHORIZATION, TokenFixture.TOKEN_PREFIX + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // given
            StudyLog studyLog = studyLogTestHelper.saveStudyLog(member, dailyGoal);
            PresignStudyLogImageRequest request = new PresignStudyLogImageRequest("test.jpg");

            // when
            ResultActions resultActions = getResultActions("", studyLog.getId(), request);

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
        @DisplayName("유효한 파일명으로 Presigned URL을 발급한다")
        void shouldIssuePresignedUrlWhenFilenameIsValid() throws Exception {
            // given
            StudyLog studyLog = studyLogTestHelper.saveStudyLog(member, dailyGoal);
            PresignStudyLogImageRequest request = new PresignStudyLogImageRequest("studylog.jpg");
            given(s3ImageStorageProvider.issuePresignedUrl(anyString()))
                    .willReturn("https://mocked-presigned-url.com");

            // when
            ResultActions resultActions = getResultActions(token, studyLog.getId(), request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.data.presignedUrl").isNotEmpty())
                    .andExpect(jsonPath("$.data.tmpKey").isNotEmpty())
                    .andExpect(
                            jsonPath("$.data.tmpKey").value(Matchers.startsWith("tmp/study-logs/")))
                    .andExpect(
                            jsonPath("$.data.tmpKey")
                                    .value(Matchers.containsString(studyLog.getId().toString())));

            // S3Provider 호출 검증
            verify(s3ImageStorageProvider).issuePresignedUrl(anyString());
        }

        @Test
        @DisplayName("파일명이 비어있으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenFilenameIsEmpty() throws Exception {
            // given
            StudyLog studyLog = studyLogTestHelper.saveStudyLog(member, dailyGoal);
            PresignStudyLogImageRequest request = new PresignStudyLogImageRequest("");

            // when
            ResultActions resultActions = getResultActions(token, studyLog.getId(), request);

            // then
            resultActions.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("유효하지 않은 확장자는 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenExtensionIsInvalid() throws Exception {
            // given
            StudyLog studyLog = studyLogTestHelper.saveStudyLog(member, dailyGoal);
            PresignStudyLogImageRequest request = new PresignStudyLogImageRequest("image.pdf");

            // when
            ResultActions resultActions = getResultActions(token, studyLog.getId(), request);

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
    }

    @Nested
    @DisplayName("학습 로그 이미지 확정 API")
    class ConfirmImage {
        private static final String CONFIRM_URL = "/api/study-logs/%d/images/confirm";

        private ResultActions getResultActions(
                String token, Long studyLogId, ConfirmStudyLogImageRequest request)
                throws Exception {
            return mockMvc.perform(
                    post(String.format(CONFIRM_URL, studyLogId))
                            .header(HttpHeaders.AUTHORIZATION, TokenFixture.TOKEN_PREFIX + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // given
            StudyLog studyLog = studyLogTestHelper.saveStudyLog(member, dailyGoal);
            ConfirmStudyLogImageRequest request =
                    new ConfirmStudyLogImageRequest("tmp/study-logs/1/test.jpg");

            // when
            ResultActions resultActions = getResultActions("", studyLog.getId(), request);

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
        @DisplayName("tmpKey가 비어있으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenTmpKeyIsEmpty() throws Exception {
            // given
            StudyLog studyLog = studyLogTestHelper.saveStudyLog(member, dailyGoal);
            ConfirmStudyLogImageRequest request = new ConfirmStudyLogImageRequest("");

            // when
            ResultActions resultActions = getResultActions(token, studyLog.getId(), request);

            // then
            resultActions.andExpect(status().isBadRequest());
        }
    }
}
