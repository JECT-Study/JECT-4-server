package com.ject.studytrip.mission.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ject.studytrip.BaseIntegrationTest;
import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.fixture.TokenFixture;
import com.ject.studytrip.auth.helper.TokenTestHelper;
import com.ject.studytrip.global.exception.error.CommonErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.helper.MemberTestHelper;
import com.ject.studytrip.mission.domain.error.MissionErrorCode;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.fixture.CreateMissionRequestFixture;
import com.ject.studytrip.mission.fixture.UpdateMissionOrderRequestFixture;
import com.ject.studytrip.mission.fixture.UpdateMissionRequestFixture;
import com.ject.studytrip.mission.helper.MissionTestHelper;
import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest;
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionOrderRequest;
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest;
import com.ject.studytrip.stamp.domain.error.StampErrorCode;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.helper.StampTestHelper;
import com.ject.studytrip.trip.domain.error.TripErrorCode;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.helper.TripTestHelper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class MissionControllerIntegrationTest extends BaseIntegrationTest {
    private static final String BASE_MISSION_URL = "/api/trips/{tripId}/stamps/{stampId}/missions";

    @Autowired private MemberTestHelper memberTestHelper;
    @Autowired private TokenTestHelper tokenTestHelper;
    @Autowired private TripTestHelper tripTestHelper;
    @Autowired private StampTestHelper stampTestHelper;
    @Autowired private MissionTestHelper missionTestHelper;

    private String accessToken;
    private Trip courseTrip;
    private Trip exploreTrip;
    private Stamp courseStamp;
    private Stamp exploreStamp;
    private Mission courseMission1;
    private Mission courseMission2;
    private Mission exploreMission1;
    private Mission exploreMission2;

    private Trip deletedTrip;
    private Stamp deletedStamp;
    private Mission deletedMission;

    private String newAccessToken;
    private Stamp newStamp;
    private Mission newMission;

    @BeforeEach
    void setUp() {
        Member member = memberTestHelper.saveMember();
        accessToken =
                tokenTestHelper.createAccessToken(
                        member.getId().toString(), member.getRole().name());
        courseTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE);
        exploreTrip = tripTestHelper.saveTrip(member, TripCategory.EXPLORE);
        courseStamp = stampTestHelper.saveStamp(courseTrip, 3);
        exploreStamp = stampTestHelper.saveStamp(exploreTrip, 0);
        courseMission1 = missionTestHelper.saveMission(courseStamp, 1);
        courseMission2 = missionTestHelper.saveMission(courseStamp, 2);
        exploreMission1 = missionTestHelper.saveMission(exploreStamp, 1);
        exploreMission2 = missionTestHelper.saveMission(exploreStamp, 2);

        deletedTrip = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE);
        deletedStamp = stampTestHelper.saveDeletedStamp(courseTrip, 3);
        deletedMission = missionTestHelper.saveDeletedMission(courseStamp, 1);

        Member newMember = memberTestHelper.saveMember("test@kakao.com", "TEST NICKNAME");
        newAccessToken =
                tokenTestHelper.createAccessToken(
                        newMember.getId().toString(), newMember.getRole().name());
        Trip newTrip = tripTestHelper.saveTrip(newMember, TripCategory.EXPLORE);
        newStamp = stampTestHelper.saveStamp(newTrip, 0);
        newMission = missionTestHelper.saveMission(newStamp, 4);
    }

    @Nested
    @DisplayName("미션 생성 API")
    class CreateMission {
        private final CreateMissionRequestFixture fixture = new CreateMissionRequestFixture();

        private ResultActions getResultActions(
                String accessToken, Object tripId, Object stampId, CreateMissionRequest request)
                throws Exception {
            return mockMvc.perform(
                    post(BASE_MISSION_URL, tripId, stampId)
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenUnauthenticated() throws Exception {
            // given
            CreateMissionRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions("", courseTrip.getId(), courseStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTripIdTypeMismatch() throws Exception {
            // given
            String invalidTripId = "abc";
            CreateMissionRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, invalidTripId, courseStamp.getId(), request);

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
        @DisplayName("PathVariable 스탬프 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenStampIdTypeMismatch() throws Exception {
            // given
            String invalidStampId = "def";
            CreateMissionRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), invalidStampId, request);

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
        @DisplayName("미션 이름이 null 이면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenNameIsNull() throws Exception {
            // given
            CreateMissionRequest request = fixture.withName(null).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), courseStamp.getId(), request);

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
        @DisplayName("미션 메모가 null 이면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenMemoIsNull() throws Exception {
            // given
            CreateMissionRequest request = fixture.withMemo(null).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), courseStamp.getId(), request);

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
        @DisplayName("미션 이름이 비어있으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
            // given
            CreateMissionRequest request = fixture.withName(" ").build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), courseStamp.getId(), request);

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
        @DisplayName("미션 메모가 비어있으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenMemoIsBlank() throws Exception {
            // given
            CreateMissionRequest request = fixture.withMemo(" ").build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), courseStamp.getId(), request);

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
        @DisplayName("미션 순서가 1 미만이면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenOrderIsLessThanOne() throws Exception {
            // given
            CreateMissionRequest request = fixture.withMissionOrder(0).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), courseStamp.getId(), request);

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
        @DisplayName("삭제된 여행일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTripAlreadyDeleted() throws Exception {
            // given
            CreateMissionRequest request = fixture.withMissionOrder(0).build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken, deletedTrip.getId(), courseStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_ALREADY_DELETED.getStatus().value()));
        }

        @Test
        @DisplayName("삭제된 스탬프일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenStampAlreadyDeleted() throws Exception {
            // given
            CreateMissionRequest request = fixture.withMissionOrder(0).build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken, courseTrip.getId(), deletedStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_ALREADY_DELETED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("이미 존재하는 미션 순서일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenMissionOrderAlreadyExists() throws Exception {
            // given
            CreateMissionRequest request = fixture.withMissionOrder(2).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), courseStamp.getId(), request);

            System.out.println(resultActions.andReturn().getResponse().getContentAsString());

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            MissionErrorCode.MISSION_ORDER_ALREADY_EXISTS
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("여행의 소유자가 아니라면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenNotTripOwner() throws Exception {
            // given
            CreateMissionRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(newAccessToken, courseTrip.getId(), newStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
        }

        @Test
        @DisplayName("스탬프가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenStampNotBelongToTrip() throws Exception {
            // given
            CreateMissionRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken, courseTrip.getId(), exploreStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_NOT_BELONG_TO_TRIP
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("존재하지 않는 여행 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenTripIdIsInvalid() throws Exception {
            // given
            Long invalidTripId = 10000L;
            CreateMissionRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, invalidTripId, courseStamp.getId(), request);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("존재하지 않는 스탬프 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenStampIdIsInvalid() throws Exception {
            // given
            Long invalidStampId = 10000L;
            CreateMissionRequest request = fixture.build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), invalidStampId, request);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(StampErrorCode.STAMP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 미션을 생성한다.")
        void shouldCreateMissionWhenRequestIsValid() throws Exception {
            // given
            CreateMissionRequest request = fixture.withMissionOrder(3).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), courseStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                    .andExpect(jsonPath("$.data.missionId").isNumber());
        }
    }

    @Nested
    @DisplayName("미션 수정 API")
    class UpdateMission {
        private final UpdateMissionRequestFixture fixture = new UpdateMissionRequestFixture();

        private ResultActions getResultActions(
                String accessToken,
                Object tripId,
                Object stampId,
                Object missionId,
                UpdateMissionRequest request)
                throws Exception {
            return mockMvc.perform(
                    patch(BASE_MISSION_URL + "/{missionId}", tripId, stampId, missionId)
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenUnauthenticated() throws Exception {
            // given
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            "",
                            courseTrip.getId(),
                            courseStamp.getId(),
                            courseMission1.getId(),
                            request);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTripIdTypeMismatch() throws Exception {
            // given
            String invalidTripId = "abc";
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            invalidTripId,
                            courseStamp.getId(),
                            courseMission1.getId(),
                            request);

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
        @DisplayName("PathVariable 스탬프 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenStampIdTypeMismatch() throws Exception {
            // given
            String invalidStampId = "def";
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            invalidStampId,
                            courseMission1.getId(),
                            request);

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
        @DisplayName("PathVariable 미션 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenMissionIdTypeMismatch() throws Exception {
            // given
            String invalidMissionId = "ghi";
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            courseStamp.getId(),
                            invalidMissionId,
                            request);

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
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            deletedTrip.getId(),
                            courseStamp.getId(),
                            courseMission1.getId(),
                            request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_ALREADY_DELETED.getStatus().value()));
        }

        @Test
        @DisplayName("삭제된 스탬프일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenStampAlreadyDeleted() throws Exception {
            // given
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            deletedStamp.getId(),
                            courseMission1.getId(),
                            request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_ALREADY_DELETED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("삭제된 미션일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenMissionAlreadyDeleted() throws Exception {
            // given
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            courseStamp.getId(),
                            deletedMission.getId(),
                            request);

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
        @DisplayName("여행의 소유자가 아니라면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenNotTripOwner() throws Exception {
            // given
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            newAccessToken,
                            courseTrip.getId(),
                            newStamp.getId(),
                            newMission.getId(),
                            request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
        }

        @Test
        @DisplayName("스탬프가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenStampNotBelongToTrip() throws Exception {
            // given
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            exploreStamp.getId(),
                            exploreMission1.getId(),
                            request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_NOT_BELONG_TO_TRIP
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("미션이 요청한 스탬프에 속하지 않으면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenMissionNotBelongToStamp() throws Exception {
            // given
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            courseStamp.getId(),
                            exploreMission1.getId(),
                            request);

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

        @Test
        @DisplayName("존재하지 않는 여행 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenTripIdIsInvalid() throws Exception {
            // given
            Long invalidTripId = 10000L;
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            invalidTripId,
                            courseStamp.getId(),
                            courseMission1.getId(),
                            request);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("존재하지 않는 스탬프 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenStampIdIsInvalid() throws Exception {
            // given
            Long invalidStampId = 10000L;
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            invalidStampId,
                            courseMission1.getId(),
                            request);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(StampErrorCode.STAMP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("존재하지 않는 미션 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenMissionIdIsInvalid() throws Exception {
            // given
            Long invalidMissionId = 10000L;
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            courseStamp.getId(),
                            invalidMissionId,
                            request);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(MissionErrorCode.MISSION_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 미션 이름을 수정한다.")
        void shouldUpdateMissionNameWhenRequestIsValid() throws Exception {
            // given
            UpdateMissionRequest request = fixture.withName("새로운 미션 이름").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            courseStamp.getId(),
                            courseMission1.getId(),
                            request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 미션 메모를 수정한다.")
        void shouldUpdateMissionMemoWhenRequestIsValid() throws Exception {
            // given
            UpdateMissionRequest request = fixture.withName("새로운 미션 메모").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            courseStamp.getId(),
                            courseMission1.getId(),
                            request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 미션 이름과 메모를 모두 수정한다.")
        void shouldUpdateMissionNameAndMemoWhenRequestIsValid() throws Exception {
            // given
            UpdateMissionRequest request =
                    fixture.withName("새로운 미션 메모").withMemo("새로운 미션 메모").build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            courseStamp.getId(),
                            courseMission1.getId(),
                            request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }
    }

    @Nested
    @DisplayName("미션 순서 변경 API")
    class UpdateMissionOrders {
        private final UpdateMissionOrderRequestFixture fixture =
                new UpdateMissionOrderRequestFixture();

        private ResultActions getResultActions(
                String accessToken,
                Object tripId,
                Object stampId,
                UpdateMissionOrderRequest request)
                throws Exception {
            return mockMvc.perform(
                    put(BASE_MISSION_URL + "/orders", tripId, stampId)
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenUnauthenticated() throws Exception {
            // given
            List<Long> ids = List.of(courseMission2.getId(), courseMission1.getId());
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions("", courseTrip.getId(), courseStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTripIdTypeMismatch() throws Exception {
            // given
            String invalidTripId = "abc";
            List<Long> ids = List.of(courseMission2.getId(), courseMission1.getId());
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, invalidTripId, courseStamp.getId(), request);

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
        @DisplayName("PathVariable 스탬프 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenStampIdTypeMismatch() throws Exception {
            // given
            String invalidStampId = "def";
            List<Long> ids = List.of(courseMission2.getId(), courseMission1.getId());
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), invalidStampId, request);

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
            List<Long> ids = List.of(courseMission2.getId(), courseMission1.getId());
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken, deletedTrip.getId(), courseStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_ALREADY_DELETED.getStatus().value()));
        }

        @Test
        @DisplayName("삭제된 스탬프일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenStampAlreadyDeleted() throws Exception {
            // given
            List<Long> ids = List.of(courseMission2.getId(), courseMission1.getId());
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken, courseTrip.getId(), deletedStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_ALREADY_DELETED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("삭제된 미션이 포함되어 있다면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenMissionAlreadyDeleted() throws Exception {
            // given
            List<Long> ids = List.of(courseMission2.getId(), deletedMission.getId());
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), courseStamp.getId(), request);

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
        @DisplayName("여행의 소유자가 아니라면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenNotTripOwner() throws Exception {
            // given
            List<Long> ids = List.of(exploreMission2.getId(), exploreMission1.getId());
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(newAccessToken, courseTrip.getId(), newStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
        }

        @Test
        @DisplayName("스탬프가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenStampNotBelongToTrip() throws Exception {
            // given
            List<Long> ids = List.of(courseMission2.getId(), courseMission1.getId());
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken, courseTrip.getId(), exploreStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_NOT_BELONG_TO_TRIP
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("미션이 요청한 스탬프에 속하지 않으면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenMissionNotBelongToStamp() throws Exception {
            // given
            List<Long> ids = List.of(exploreMission2.getId(), courseMission1.getId());
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), courseStamp.getId(), request);

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

        @Test
        @DisplayName("존재하지 않는 여행 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenTripIdIsInvalid() throws Exception {
            // given
            Long invalidTripId = 10000L;
            List<Long> ids = List.of(courseMission2.getId(), courseMission1.getId());
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, invalidTripId, courseStamp.getId(), request);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("존재하지 않는 스탬프 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenStampIdIsInvalid() throws Exception {
            // given
            Long invalidStampId = 10000L;
            List<Long> ids = List.of(courseMission2.getId(), courseMission1.getId());
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), invalidStampId, request);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(StampErrorCode.STAMP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("중복된 미션 ID가 들어오면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenDuplicatedIds() throws Exception {
            // given
            List<Long> duplicatedIds = List.of(courseMission1.getId(), courseMission1.getId());
            UpdateMissionOrderRequest request = fixture.withOrderedIds(duplicatedIds).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), courseStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            MissionErrorCode.MISSION_ORDER_IDS_DUPLICATED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("요청된 미션 ID 개수가 실제 미션 개수와 다르면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenSizeMismatch() throws Exception {
            // given
            Long invalidMissionId = 10000L;
            List<Long> ids = List.of(courseMission1.getId(), invalidMissionId);
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), courseStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            MissionErrorCode.MISSION_ORDER_SIZE_MISMATCHED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("요청된 미션 ID 목록에 존재하지 않는 ID가 포함되어 있으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenIdsContainInvalidMissionId() throws Exception {
            // given
            Long invalidMissionId = 10000L;
            List<Long> ids = List.of(courseMission1.getId(), invalidMissionId);
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), courseStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            MissionErrorCode.MISSION_ORDER_IDS_NOT_MATCHED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 미션 순서를 변경한다.")
        void shouldUpdateMissionOrdersWhenRequestIsValid() throws Exception {
            // given
            List<Long> ids = List.of(courseMission2.getId(), courseMission1.getId());
            UpdateMissionOrderRequest request = fixture.withOrderedIds(ids).build();

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), courseStamp.getId(), request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }
    }

    @Nested
    @DisplayName("미션 삭제 API")
    class DeleteMission {
        private ResultActions getResultActions(
                String accessToken, Object tripId, Object stampId, Object missionId)
                throws Exception {
            return mockMvc.perform(
                    delete(BASE_MISSION_URL + "/{missionId}", tripId, stampId, missionId)
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken));
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenUnauthenticated() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(
                            "", exploreTrip.getId(), exploreStamp.getId(), exploreMission1.getId());

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTripIdTypeMismatch() throws Exception {
            // given
            String invalidTripId = "abc";

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            invalidTripId,
                            exploreStamp.getId(),
                            exploreMission1.getId());

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
        @DisplayName("PathVariable 스탬프 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenStampIdTypeMismatch() throws Exception {
            // given
            String invalidStampId = "def";

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            exploreTrip.getId(),
                            invalidStampId,
                            exploreMission1.getId());

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
        @DisplayName("PathVariable 미션 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenMissionIdTypeMismatch() throws Exception {
            // given
            String invalidMissionId = "ghi";

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            exploreTrip.getId(),
                            exploreStamp.getId(),
                            invalidMissionId);

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
            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            deletedTrip.getId(),
                            courseStamp.getId(),
                            courseMission2.getId());

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_ALREADY_DELETED.getStatus().value()));
        }

        @Test
        @DisplayName("삭제된 스탬프일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenStampAlreadyDeleted() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            deletedStamp.getId(),
                            courseMission2.getId());

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_ALREADY_DELETED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("삭제된 미션일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenMissionAlreadyDeleted() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            courseStamp.getId(),
                            deletedMission.getId());

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
        @DisplayName("여행의 소유자가 아니라면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenNotTripOwner() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(
                            newAccessToken,
                            exploreTrip.getId(),
                            newStamp.getId(),
                            newMission.getId());

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
        }

        @Test
        @DisplayName("스탬프가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenStampNotBelongToTrip() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            exploreStamp.getId(),
                            exploreMission1.getId());

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_NOT_BELONG_TO_TRIP
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("미션이 요청한 스탬프에 속하지 않으면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenMissionNotBelongToStamp() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            courseTrip.getId(),
                            courseStamp.getId(),
                            exploreMission1.getId());

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

        @Test
        @DisplayName("존재하지 않는 여행 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenTripIdIsInvalid() throws Exception {
            // given
            Long invalidTripId = 10000L;
            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            invalidTripId,
                            exploreStamp.getId(),
                            exploreMission2.getId());

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("존재하지 않는 스탬프 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenStampIdIsInvalid() throws Exception {
            // given
            Long invalidStampId = 10000L;

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            exploreTrip.getId(),
                            invalidStampId,
                            exploreMission2.getId());

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(StampErrorCode.STAMP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("존재하지 않는 미션 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenMissionIdIsInvalid() throws Exception {
            // given
            Long invalidMissionId = 10000L;

            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            exploreTrip.getId(),
                            exploreStamp.getId(),
                            invalidMissionId);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(MissionErrorCode.MISSION_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 미션을 삭제한다.")
        void shouldDeleteMissionWhenRequestIsValid() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(
                            accessToken,
                            exploreTrip.getId(),
                            exploreStamp.getId(),
                            exploreMission2.getId());

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }
    }

    @Nested
    @DisplayName("미션 목록 조회 API")
    class LoadMissionsByStamp {
        private ResultActions getResultActions(String accessToken, Object tripId, Object stampId)
                throws Exception {
            return mockMvc.perform(
                    get(BASE_MISSION_URL, tripId, stampId)
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken));
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenUnauthenticated() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions("", exploreTrip.getId(), exploreStamp.getId());

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTripIdTypeMismatch() throws Exception {
            // given
            String invalidTripId = "abc";

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, invalidTripId, exploreStamp.getId());

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
        @DisplayName("PathVariable 스탬프 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenStampIdTypeMismatch() throws Exception {
            // given
            String invalidStampId = "def";

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, exploreTrip.getId(), invalidStampId);

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
            // when
            ResultActions resultActions =
                    getResultActions(accessToken, deletedTrip.getId(), courseStamp.getId());

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_ALREADY_DELETED.getStatus().value()));
        }

        @Test
        @DisplayName("삭제된 스탬프일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenStampAlreadyDeleted() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), deletedStamp.getId());

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_ALREADY_DELETED
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("여행의 소유자가 아니라면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenNotTripOwner() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(newAccessToken, exploreTrip.getId(), newStamp.getId());

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
        }

        @Test
        @DisplayName("스탬프가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenStampNotBelongToTrip() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(accessToken, courseTrip.getId(), exploreStamp.getId());

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_NOT_BELONG_TO_TRIP
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("존재하지 않는 여행 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenTripIdIsInvalid() throws Exception {
            // given
            Long invalidTripId = 10000L;

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, invalidTripId, exploreStamp.getId());

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("존재하지 않는 스탬프 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenStampIdIsInvalid() throws Exception {
            // given
            Long invalidStampId = 10000L;

            // when
            ResultActions resultActions =
                    getResultActions(accessToken, exploreTrip.getId(), invalidStampId);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(StampErrorCode.STAMP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 미션 목록을 조회한다.")
        void shouldLoadMissionsByStampWhenRequestIsValid() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(accessToken, exploreTrip.getId(), exploreStamp.getId());

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }
    }
}
