package com.ject.studytrip.trip.presentation.controller;

import static com.ject.studytrip.auth.fixture.TokenFixture.TOKEN_PREFIX;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ject.studytrip.BaseIntegrationTest;
import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.helper.TokenTestHelper;
import com.ject.studytrip.global.exception.error.CommonErrorCode;
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
import com.ject.studytrip.pomodoro.presentation.dto.request.CreatePomodoroRequest;
import com.ject.studytrip.stamp.domain.error.StampErrorCode;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.helper.StampTestHelper;
import com.ject.studytrip.trip.domain.error.DailyGoalErrorCode;
import com.ject.studytrip.trip.domain.error.TripErrorCode;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.CreateDailyGoalRequestFixture;
import com.ject.studytrip.trip.fixture.UpdateDailyGoalRequestFixture;
import com.ject.studytrip.trip.helper.DailyGoalTestHelper;
import com.ject.studytrip.trip.helper.TripTestHelper;
import com.ject.studytrip.trip.presentation.dto.request.CreateDailyGoalRequest;
import com.ject.studytrip.trip.presentation.dto.request.UpdateDailyGoalRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("DailyGoalController 통합 테스트")
public class DailyGoalControllerIntegrationTest extends BaseIntegrationTest {
    @Autowired private MemberTestHelper memberTestHelper;
    @Autowired private TripTestHelper tripTestHelper;
    @Autowired private StampTestHelper stampTestHelper;
    @Autowired private MissionTestHelper missionTestHelper;
    @Autowired private DailyGoalTestHelper dailyGoalTestHelper;
    @Autowired private PomodoroTestHelper pomodoroTestHelper;
    @Autowired private DailyMissionTestHelper dailyMissionTestHelper;
    @Autowired private TokenTestHelper tokenTestHelper;

    private Member member;
    private Trip trip;
    private Stamp stamp;
    private Mission firstMission;
    private Mission secondMission;
    private DailyGoal dailyGoal;
    private DailyMission dailyMission;
    private String token;

    @BeforeEach
    void setUp() {
        member = memberTestHelper.saveMember();
        trip = tripTestHelper.saveTrip(member, TripCategory.COURSE);
        stamp = stampTestHelper.saveStamp(trip, 1);
        firstMission = missionTestHelper.saveMission(stamp);
        secondMission = missionTestHelper.saveMission(stamp);
        dailyGoal = dailyGoalTestHelper.saveDailyGoal(trip);
        dailyMission = dailyMissionTestHelper.saveDailyMission(firstMission, dailyGoal);
        token =
                tokenTestHelper.createAccessToken(
                        member.getId().toString(), MemberRole.ROLE_USER.name());
    }

    @Nested
    @DisplayName("데일리 목표 생성 API")
    class CreateDailyGoal {
        private final CreateDailyGoalRequestFixture fixture = new CreateDailyGoalRequestFixture();

        private ResultActions getResultActions(
                String token, Object tripId, CreateDailyGoalRequest request) throws Exception {
            return mockMvc.perform(
                    post("/api/trips/{tripId}/daily-goals", tripId)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("유효한 요청으로 데일리 목표와 뽀모도로, 데일리 미션을 함께 생성한다")
        void shouldCreateDailyGoalWithPomodoroAndDailyMissions() throws Exception {
            // given
            CreateDailyGoalRequest request =
                    fixture.withMissionIds(List.of(firstMission.getId(), secondMission.getId()))
                            .build();

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), request);

            // then
            resultActions
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.dailyGoalId").isNumber());
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 Unauthorized를 반환한다")
        void shouldReturnUnauthorizedWhenUnauthenticated() throws Exception {
            // given
            CreateDailyGoalRequest request = fixture.build();

            // when
            ResultActions resultActions = getResultActions("", trip.getId(), request);

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
            CreateDailyGoalRequest request = fixture.build();
            // when
            ResultActions resultActions = getResultActions(token, tripId, request);

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
        @DisplayName("데일리 목표를 생성하는데 필요한 필수 요청 값이 누락되거나 유효하지 않으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenInvalidRequiredFields() throws Exception {
            // given
            CreateDailyGoalRequest request =
                    fixture.withPomodoro(null).withMissionIds(List.of()).build();
            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), request);

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
        @DisplayName("데일리 목표의 뽀모도로 정보 중 집중 시간이 1분 미만이면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenDailyGoalPomodoroFocusTimeInMinuteIsLessThanOneMinute()
                throws Exception {
            // given
            CreateDailyGoalRequest request =
                    fixture.withPomodoro(new CreatePomodoroRequest(0, 1)).build();
            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), request);

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
        @DisplayName("데일리 목표의 뽀모도로 정보 중 집중 세션이 1개 미만이면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenDailyGoalPomodoroFocusSessionCountIsLessThanOne()
                throws Exception {
            // given
            CreateDailyGoalRequest request =
                    fixture.withPomodoro(new CreatePomodoroRequest(30, 0)).build();
            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), request);

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
            CreateDailyGoalRequest request = fixture.build();

            // when
            ResultActions resultActions = getResultActions(token, tripId, request);

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
            CreateDailyGoalRequest request = fixture.build();

            // when
            ResultActions resultActions = getResultActions(token, newTrip.getId(), request);

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
            CreateDailyGoalRequest request = fixture.build();

            // when
            ResultActions resultActions = getResultActions(token, deleted.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_ALREADY_DELETED.getStatus().value()));
        }

        @Test
        @DisplayName("요청한 미션 ID 목록으로 해당 미션을 조회하고, 하나라도 일치하지 않는 경우 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenAnyMissionIdDoesNotExist() throws Exception {
            // given
            CreateDailyGoalRequest request =
                    fixture.withMissionIds(List.of(firstMission.getId(), 1000L)).build();

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), request);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(MissionErrorCode.MISSION_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("코스형 여행에서 현재 진행중인 스탬프(완료되지 않은 가장 첫번째 스탬프)가 없을 경우 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenNotFoundWhenNoIncompleteStampExists() throws Exception {
            // given
            Trip newTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE);
            stampTestHelper.saveCompletedStamp(newTrip, 1);
            CreateDailyGoalRequest request = fixture.build();

            // when
            ResultActions resultActions = getResultActions(token, newTrip.getId(), request);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(StampErrorCode.STAMP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("조회된 미션들의 스탬프 정보를 확인해 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다")
        void shouldReturnForbiddenWhenMissionsStampsDoesNotBelongToTrip() throws Exception {
            // given
            Trip newTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE);
            stampTestHelper.saveStamp(newTrip, 1);
            CreateDailyGoalRequest request =
                    fixture.withMissionIds(List.of(firstMission.getId())).build();

            // when
            ResultActions resultActions = getResultActions(token, newTrip.getId(), request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("조회된 미션들 중 삭제된 미션이 존재하면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenMissionIsDeleted() throws Exception {
            // given
            Mission deleted = missionTestHelper.saveDeletedMission(stamp);
            CreateDailyGoalRequest request =
                    fixture.withMissionIds(List.of(deleted.getId())).build();

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), request);

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
        @DisplayName("조회된 미션들 중 완료된 미션이 존재하면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenMissionIsAlreadyCompleted() throws Exception {
            // given
            Mission completed = missionTestHelper.saveCompletedMission(stamp);
            CreateDailyGoalRequest request =
                    fixture.withMissionIds(List.of(completed.getId())).build();

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), request);

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

        @Test
        @DisplayName("코스형 여행에서 조회된 미션들이 현재 진행중인 스탬프에 속하지 않은 경우 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenMissionNotBelongToCurrentStamp() throws Exception {
            // given
            Stamp newStamp = stampTestHelper.saveStamp(trip, 2);
            Mission newMission = missionTestHelper.saveMission(newStamp);
            CreateDailyGoalRequest request =
                    fixture.withMissionIds(List.of(newMission.getId())).build();

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP
                                                    .getStatus()
                                                    .value()));
        }
    }

    @Nested
    @DisplayName("데일리 목표 수정 API")
    class UpdateDailyGoal {
        private final UpdateDailyGoalRequestFixture fixture = new UpdateDailyGoalRequestFixture();

        private ResultActions getResultActions(
                String token, Object tripId, Object dailyGoalId, UpdateDailyGoalRequest request)
                throws Exception {
            return mockMvc.perform(
                    patch("/api/trips/{tripId}/daily-goals/{dailyGoalId}", tripId, dailyGoalId)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("유효한 요청으로 특정 데일리 목표에 속한 데일리 미션을 수정한다")
        void shouldUpdateDailyGoal() throws Exception {
            // given
            Mission addMission = missionTestHelper.saveMission(stamp);
            UpdateDailyGoalRequest request =
                    fixture.withDeleteDailyMissionIds(List.of(dailyMission.getId()))
                            .withAddMissionIds(List.of(addMission.getId()))
                            .build();

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), dailyGoal.getId(), request);

            // then
            resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 Unauthorized를 반환한다")
        void shouldReturnUnauthorizedWhenUnauthenticated() throws Exception {
            // given
            UpdateDailyGoalRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions("", trip.getId(), dailyGoal.getId(), request);

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
            UpdateDailyGoalRequest request = fixture.build();

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
            UpdateDailyGoalRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), dailyGoalId, request);

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
        @DisplayName("유효하지 않은 여행 ID 라면 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenInvalidTripId() throws Exception {
            // given
            Long tripId = 10000L;
            UpdateDailyGoalRequest request = fixture.build();

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
            UpdateDailyGoalRequest request = fixture.build();

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
            UpdateDailyGoalRequest request = fixture.build();

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
            UpdateDailyGoalRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), dailyGoalId, request);

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
            UpdateDailyGoalRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), newDailyGoal.getId(), request);

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
            DailyGoal deleted = dailyGoalTestHelper.saveDeletedDailyGoal(trip);
            UpdateDailyGoalRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), deleted.getId(), request);

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
        @DisplayName("삭제를 요청한 데일리 미션 ID 개수와 조회된 데일리 미션 개수가 다르면 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenAnyDeleteTargetDailyMissionDoesNotExist() throws Exception {
            // given
            List<Long> ids = List.of(dailyMission.getId(), 1000L);
            UpdateDailyGoalRequest request = fixture.withDeleteDailyMissionIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), dailyGoal.getId(), request);

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
        @DisplayName("삭제를 요청한 데일리 미션이 요청한 데일리 목표에 속하지 않으면 403 Forbidden을 반환한다")
        void shouldReturnForbiddenWhenDeleteTargetDailyMissionDoesNotBelongToDailyGoal()
                throws Exception {
            // given
            DailyGoal newDailyGoal = dailyGoalTestHelper.saveDailyGoal(trip);
            DailyMission newDailyMission =
                    dailyMissionTestHelper.saveDailyMission(firstMission, newDailyGoal);
            UpdateDailyGoalRequest request =
                    fixture.withDeleteDailyMissionIds(List.of(newDailyMission.getId())).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), dailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            DailyMissionErrorCode
                                                    .DAILY_MISSION_NOT_BELONGS_TO_DAILY_GOAL
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("데일리 미션이 이미 삭제된 경우 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenDeleteTargetDailyMissionIsAlreadyDeleted() throws Exception {
            // given
            dailyMission.updateDeletedAt();
            UpdateDailyGoalRequest request =
                    fixture.withDeleteDailyMissionIds(List.of(dailyMission.getId())).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), dailyGoal.getId(), request);

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
        @DisplayName("추가할 미션 ID 목록으로 해당 미션을 조회하고, 하나라도 일치하지 않는 경우 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenAnyAddMissionIdDoesNotExist() throws Exception {
            // given
            UpdateDailyGoalRequest request =
                    fixture.withAddMissionIds(List.of(100L, 200L, 300L)).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), dailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(MissionErrorCode.MISSION_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("코스형 여행에서 현재 진행중인 스탬프(완료되지 않은 가장 첫번째 스탬프)가 없을 경우 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenNoIncompleteStampExists() throws Exception {
            // given
            Trip newTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE);
            stampTestHelper.saveCompletedStamp(newTrip, 1);
            DailyGoal newDailyGoal = dailyGoalTestHelper.saveDailyGoal(newTrip);
            UpdateDailyGoalRequest request = fixture.withAddMissionIds(List.of(1L)).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, newTrip.getId(), newDailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(StampErrorCode.STAMP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("새로 추가할 미션들의 스탬프 정보를 확인해 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다")
        void shouldReturnForbiddenWhenAddMissionsStampsDoesNotBelongToTrip() throws Exception {
            // given
            Trip newTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE);
            Stamp newStamp = stampTestHelper.saveStamp(newTrip, 1);
            Mission newMission = missionTestHelper.saveMission(newStamp);
            UpdateDailyGoalRequest request =
                    fixture.withAddMissionIds(List.of(newMission.getId())).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), dailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("새로 추가할 미션들 중 삭제된 미션이 존재하면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenAnyAddMissionIsDeleted() throws Exception {
            // given
            Mission deleted = missionTestHelper.saveDeletedMission(stamp);
            UpdateDailyGoalRequest request =
                    fixture.withAddMissionIds(List.of(deleted.getId())).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), dailyGoal.getId(), request);

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
        @DisplayName("새로 추가할 미션들 중 완료된 미션이 존재하면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenAnyAddMissionIsAlreadyCompleted() throws Exception {
            // given
            Mission completed = missionTestHelper.saveCompletedMission(stamp);
            UpdateDailyGoalRequest request =
                    fixture.withAddMissionIds(List.of(completed.getId())).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), dailyGoal.getId(), request);

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

        @Test
        @DisplayName("코스형 여행에서 새로 추가한 미션들이 현재 진행중인 스탬프에 속하지 않은 경우 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenAddMissionsDoNotBelongToCurrentStamp() throws Exception {
            // given
            Stamp newStamp = stampTestHelper.saveStamp(trip, 2);
            Mission newMission = missionTestHelper.saveMission(newStamp);
            UpdateDailyGoalRequest request =
                    fixture.withAddMissionIds(List.of(newMission.getId())).build();

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), dailyGoal.getId(), request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP
                                                    .getStatus()
                                                    .value()));
        }
    }

    @Nested
    @DisplayName("데일리 목표 삭제 API")
    class DeleteDailyGoal {

        private ResultActions getResultActions(String token, Object tripId, Object dailyGoalId)
                throws Exception {
            return mockMvc.perform(
                    delete("/api/trips/{tripId}/daily-goals/{dailyGoalId}", tripId, dailyGoalId)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token));
        }

        @Test
        @DisplayName("유효한 요청으로 데일리 목표와 뽀모도로, 데일리 미션을 삭제한다")
        void shouldDeleteDailyGoalAndPomodoroAndDailyMissions() throws Exception {
            // given
            pomodoroTestHelper.savePomodoro(dailyGoal);

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), dailyGoal.getId());

            // then
            resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 Unauthorized를 반환한다")
        void shouldReturnUnauthorizedWhenUnauthenticated() throws Exception {
            // when
            ResultActions resultActions = getResultActions("", trip.getId(), dailyGoal.getId());

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
            ResultActions resultActions = getResultActions(token, tripId, dailyGoal.getId());

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

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), dailyGoalId);

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
        @DisplayName("유효하지 않은 여행 ID 라면 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenInvalidTripId() throws Exception {
            // given
            Long tripId = 10000L;

            // when
            ResultActions resultActions = getResultActions(token, tripId, dailyGoal.getId());

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
                    getResultActions(token, newTrip.getId(), dailyGoal.getId());

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
                    getResultActions(token, deleted.getId(), dailyGoal.getId());

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

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), dailyGoalId);

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

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), newDailyGoal.getId());

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
            DailyGoal deleted = dailyGoalTestHelper.saveDeletedDailyGoal(trip);

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), deleted.getId());

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
        @DisplayName("데일리 목표 ID로 뽀모도로를 조회하고 존재하지 않을 경우 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenPomodoroDoesNotExist() throws Exception {
            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), dailyGoal.getId());

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
        @DisplayName("데일리 목표 ID로 뽀모도로를 조회하고 이미 삭제된 경우 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenPomodoroIsAlreadyDeleted() throws Exception {
            // given
            pomodoroTestHelper.saveDeletedPomodoro(dailyGoal);

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), dailyGoal.getId());

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
    }

    @Nested
    @DisplayName("데일리 목표 조회 API")
    class GetDailyGoal {

        private ResultActions getResultActions(String token, Object tripId, Object dailyGoalId)
                throws Exception {
            return mockMvc.perform(
                    get("/api/trips/{tripId}/daily-goals/{dailyGoalId}", tripId, dailyGoalId)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token));
        }

        @Test
        @DisplayName("유효한 정보로 특정 데일리 목표를 조회하고 해당 뽀모도로와 데일리 미션을 함께 반환한다")
        void shouldReturnDailyGoal() throws Exception {
            // given
            Pomodoro pomodoro = pomodoroTestHelper.savePomodoro(dailyGoal);

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), dailyGoal.getId());

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.dailyGoalId").value(dailyGoal.getId()))
                    .andExpect(jsonPath("$.data.pomodoro.pomodoroId").value(pomodoro.getId()))
                    .andExpect(jsonPath("$.data.dailyMissions").isNotEmpty());
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 Unauthorized를 반환한다")
        void shouldReturnUnauthorizedWhenUnauthenticated() throws Exception {
            // when
            ResultActions resultActions = getResultActions("", trip.getId(), dailyGoal.getId());

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
            ResultActions resultActions = getResultActions(token, tripId, dailyGoal.getId());

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

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), dailyGoalId);

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
        @DisplayName("유효하지 않은 여행 ID 라면 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenInvalidTripId() throws Exception {
            // given
            Long tripId = 10000L;

            // when
            ResultActions resultActions = getResultActions(token, tripId, dailyGoal.getId());

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
                    getResultActions(token, newTrip.getId(), dailyGoal.getId());

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
                    getResultActions(token, deleted.getId(), dailyGoal.getId());

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

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), dailyGoalId);

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

            // when
            ResultActions resultActions =
                    getResultActions(token, trip.getId(), newDailyGoal.getId());

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
            DailyGoal deleted = dailyGoalTestHelper.saveDeletedDailyGoal(trip);

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), deleted.getId());

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
        @DisplayName("데일리 목표 ID로 뽀모도로를 조회하고 존재하지 않을 경우 404 NotFound를 반환한다")
        void shouldReturnNotFoundWhenPomodoroDoesNotExist() throws Exception {
            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), dailyGoal.getId());

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
        @DisplayName("데일리 목표 ID로 뽀모도로를 조회하고 이미 삭제된 경우 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenPomodoroIsAlreadyDeleted() throws Exception {
            // given
            pomodoroTestHelper.saveDeletedPomodoro(dailyGoal);

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), dailyGoal.getId());

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
    }
}
