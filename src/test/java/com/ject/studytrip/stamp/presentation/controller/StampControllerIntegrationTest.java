package com.ject.studytrip.stamp.presentation.controller;

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
import com.ject.studytrip.mission.helper.MissionTestHelper;
import com.ject.studytrip.stamp.domain.error.StampErrorCode;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.fixture.CreateStampRequestFixture;
import com.ject.studytrip.stamp.fixture.UpdateStampRequestFixture;
import com.ject.studytrip.stamp.helper.StampTestHelper;
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest;
import com.ject.studytrip.trip.domain.error.TripErrorCode;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.helper.TripTestHelper;
import java.util.List;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("StampController 통합 테스트")
public class StampControllerIntegrationTest extends BaseIntegrationTest {
    private static final int NEXT_STAMP_ORDER = 3;

    @Autowired private MemberTestHelper memberTestHelper;
    @Autowired private TripTestHelper tripTestHelper;
    @Autowired private StampTestHelper stampTestHelper;
    @Autowired private MissionTestHelper missionTestHelper;
    @Autowired private TokenTestHelper tokenTestHelper;

    private String token;
    private Member member;
    private Trip courseTrip;
    private Trip exploreTrip;
    private Stamp courseStamp1;
    private Stamp courseStamp2;
    private Mission courseMission1;
    private Mission courseMission2;

    private String newToken;

    @BeforeEach
    void setup() {
        member = memberTestHelper.saveMember();
        token =
                tokenTestHelper.createAccessToken(
                        member.getId().toString(), member.getRole().name());
        courseTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE);
        exploreTrip = tripTestHelper.saveTrip(member, TripCategory.EXPLORE);
        courseStamp1 = stampTestHelper.saveStamp(courseTrip, 1);
        courseStamp2 = stampTestHelper.saveStamp(courseTrip, 2);
        courseMission1 = missionTestHelper.saveMission(courseStamp1);
        courseMission2 = missionTestHelper.saveMission(courseStamp1);

        Member newMember = memberTestHelper.saveMember("test@kakao.com", "TEST NICKNAME");
        newToken =
                tokenTestHelper.createAccessToken(
                        newMember.getId().toString(), newMember.getRole().name());
    }

    @Nested
    @DisplayName("스탬프 생성 API")
    class CreateStamp {
        private final CreateStampRequestFixture createStampRequestFixture =
                new CreateStampRequestFixture();

        private ResultActions getResultActions(
                String token, Object tripId, CreateStampRequest request) throws Exception {
            return mockMvc.perform(
                    post("/api/trips/{tripId}/stamps", tripId)
                            .header(HttpHeaders.AUTHORIZATION, TokenFixture.TOKEN_PREFIX + token)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("유효한 요청으로 특정 여행의 스탬프를 생성하고, 여행 총 스탬프 수가 증가한다")
        void shouldCreateStamp() throws Exception {
            // given
            CreateStampRequest request =
                    createStampRequestFixture.withStampOrder(NEXT_STAMP_ORDER).build();

            // when
            ResultActions resultActions = getResultActions(token, courseTrip.getId(), request);

            // then
            resultActions
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.stampId").isNumber());
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 예외가 발생한다")
        void shouldThrowExceptionWhenUnauthenticated() throws Exception {
            // given
            CreateStampRequest request = createStampRequestFixture.build();

            // when
            ResultActions resultActions = getResultActions("", courseTrip.getId(), request);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenTripIdTypeMismatch() throws Exception {
            // given
            String tripId = "abc";
            CreateStampRequest request = createStampRequestFixture.build();
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
        @DisplayName("스탬프를 생성하는데 필요한 필수 요청 값이 누락되거나 유효하지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidRequiredFields() throws Exception {
            // given
            CreateStampRequest request = createStampRequestFixture.withName("").build();
            // when
            ResultActions resultActions = getResultActions(token, courseTrip.getId(), request);

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
        @DisplayName("유효하지 않은 여행 ID 라면 404 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidTripId() throws Exception {
            // given
            Long tripId = 10000L;
            CreateStampRequest request = createStampRequestFixture.build();

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
        @DisplayName("요청한 여행의 소유자가 아닐 경우 403 예외가 발생한다")
        void shouldThrowExceptionWhenNotTripOwner() throws Exception {
            // given
            Member newMember = memberTestHelper.saveMember("test@gmail.com", "TEST");
            Trip newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE);
            CreateStampRequest request = createStampRequestFixture.build();

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
        @DisplayName("삭제된 여행일 경우 400 예외가 발생한다")
        void shouldThrowExceptionWhenAlreadyTrip() throws Exception {
            // given
            Trip deleted = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE);
            CreateStampRequest request = createStampRequestFixture.build();

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
        @DisplayName("탐험형 여행에 순서가 존재하는 스탬프를 추가하면 400 예외가 발생한다")
        void shouldThrowExceptionWhenStampOrderExistsInExplorationTrip() throws Exception {
            // given
            CreateStampRequest request = createStampRequestFixture.build();

            // when
            ResultActions resultActions = getResultActions(token, exploreTrip.getId(), request);

            // when & then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.INVALID_STAMP_ORDER_FOR_EXPLORATION_TRIP
                                                    .getStatus()
                                                    .value()));
        }

        @Test
        @DisplayName("코스형 여행에 유효하지 않은 순서(중복, 범위 이탈)가 존재하는 스탬프를 추가하면 400 예외가 발생한다")
        void shouldThrowExceptionWhenStampOrderOutOfRangeInCourseTrip() throws Exception {
            // given
            CreateStampRequest request = createStampRequestFixture.build();

            // when
            ResultActions resultActions = getResultActions(token, courseTrip.getId(), request);

            // when & then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.INVALID_STAMP_ORDER_RANGE_FOR_COURSE_TRIP
                                                    .getStatus()
                                                    .value()));
        }
    }

    @Nested
    @DisplayName("스탬프 수정 API")
    class UpdateStamp {
        private final UpdateStampRequestFixture updateStampRequestFixture =
                new UpdateStampRequestFixture();

        @Nested
        @DisplayName("스탬프 이름 수정")
        class UpdateName {
            private ResultActions getResultActions(
                    String token, Object tripId, Object stampId, UpdateStampRequest request)
                    throws Exception {
                return mockMvc.perform(
                        patch("/api/trips/{tripId}/stamps/{stampId}", tripId, stampId)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        TokenFixture.TOKEN_PREFIX + token)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request)));
            }

            @Test
            @DisplayName("유효한 요청으로 스탬프의 이름을 수정한다")
            void shouldUpdateStampName() throws Exception {
                // given
                UpdateStampRequest request = updateStampRequestFixture.buildUpdateName();

                // when
                ResultActions resultActions =
                        getResultActions(token, courseTrip.getId(), courseStamp1.getId(), request);

                // then
                resultActions
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }

            @Test
            @DisplayName("인증되지 않은 사용자일 경우 401 예외가 발생한다")
            void shouldThrowExceptionWhenUnauthenticated() throws Exception {
                // given
                UpdateStampRequest request = updateStampRequestFixture.buildUpdateName();
                // when
                ResultActions resultActions =
                        getResultActions("", courseTrip.getId(), courseStamp1.getId(), request);

                // then
                resultActions
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(
                                jsonPath("$.status")
                                        .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
            }

            @Test
            @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 예외가 발생한다")
            void shouldThrowExceptionWhenTripIdTypeMismatch() throws Exception {
                // given
                String tripId = "abc";
                UpdateStampRequest request = updateStampRequestFixture.buildUpdateName();

                // when
                ResultActions resultActions =
                        getResultActions(token, tripId, courseStamp1.getId(), request);

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
            @DisplayName("PathVariable 스탬프 ID 타입이 올바르지 않으면 400 예외가 발생한다")
            void shouldThrowExceptionWhenStampIdTypeMismatch() throws Exception {
                // given
                String stampId = "abc";
                UpdateStampRequest request = updateStampRequestFixture.buildUpdateName();
                // when
                ResultActions resultActions =
                        getResultActions(token, courseTrip.getId(), stampId, request);

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
            @DisplayName("유효하지 않은 여행 ID 라면 404 예외가 발생한다")
            void shouldThrowExceptionWhenInvalidTripId() throws Exception {
                // given
                Long tripId = 10000L;
                UpdateStampRequest request = updateStampRequestFixture.buildUpdateName();

                // when
                ResultActions resultActions =
                        getResultActions(token, tripId, courseStamp1.getId(), request);

                // when & then
                resultActions
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(
                                jsonPath("$.status")
                                        .value(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
            }

            @Test
            @DisplayName("요청한 사용자가 여행의 소유자가 아닐 경우 403 예외가 발생한다")
            void shouldThrowExceptionWhenNotTripOwner() throws Exception {
                // given
                Member newMember = memberTestHelper.saveMember("test@gmail.com", "TEST");
                Trip newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE);
                Stamp newStamp = stampTestHelper.saveStamp(newTrip, 1);
                UpdateStampRequest request = updateStampRequestFixture.buildUpdateName();

                // when
                ResultActions resultActions =
                        getResultActions(token, newTrip.getId(), newStamp.getId(), request);

                // then
                resultActions
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(
                                jsonPath("$.status")
                                        .value(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
            }

            @Test
            @DisplayName("삭제된 여행일 경우 400 예외가 발생한다")
            void shouldThrowExceptionWhenAlreadyDeletedTrip() throws Exception {
                // given
                Trip deleted = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE);
                UpdateStampRequest request = updateStampRequestFixture.buildUpdateName();

                // when
                ResultActions resultActions =
                        getResultActions(token, deleted.getId(), courseStamp1.getId(), request);

                // then
                resultActions
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(
                                jsonPath("$.status")
                                        .value(
                                                TripErrorCode.TRIP_ALREADY_DELETED
                                                        .getStatus()
                                                        .value()));
            }

            @Test
            @DisplayName("유효하지 않은 스탬프 ID 라면 404 예외가 발생한다")
            void shouldThrowExceptionWhenInvalidStampId() throws Exception {
                // given
                Long stampId = 10000L;
                UpdateStampRequest request = updateStampRequestFixture.buildUpdateName();

                // when
                ResultActions resultActions =
                        getResultActions(token, courseTrip.getId(), stampId, request);

                // when & then
                resultActions
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(
                                jsonPath("$.status")
                                        .value(StampErrorCode.STAMP_NOT_FOUND.getStatus().value()));
            }

            @Test
            @DisplayName("여행에 속한 스탬프가 아닌 경우 403 예외가 발생한다")
            void shouldThrowExceptionWhenStampTripMisMatch() throws Exception {
                // given
                Stamp newStamp = stampTestHelper.saveStamp(exploreTrip, 0);
                UpdateStampRequest request = updateStampRequestFixture.buildUpdateName();

                // when
                ResultActions resultActions =
                        getResultActions(token, courseTrip.getId(), newStamp.getId(), request);

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
            @DisplayName("삭제된 스탬프일 경우 400 예외가 발생한다")
            void shouldThrowExceptionWhenAlreadyDeletedStamp() throws Exception {
                // given
                Stamp newStamp = stampTestHelper.saveDeletedStamp(exploreTrip, 0);
                UpdateStampRequest request = updateStampRequestFixture.buildUpdateName();

                // when
                ResultActions resultActions =
                        getResultActions(token, exploreTrip.getId(), newStamp.getId(), request);

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
        }

        @Nested
        @DisplayName("스탬프 순서 수정")
        class UpdateOrders {

            private ResultActions getResultActions(
                    String token, Object tripId, UpdateStampOrderRequest request) throws Exception {
                return mockMvc.perform(
                        put("/api/trips/{tripId}/stamps/orders", tripId)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        TokenFixture.TOKEN_PREFIX + token)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request)));
            }

            @Test
            @DisplayName("유효한 요청으로 스탬프의 순서를 수정한다")
            void shouldUpdateStampOrders() throws Exception {
                // given
                UpdateStampOrderRequest request =
                        new UpdateStampRequestFixture()
                                .withOrderedStampIds(
                                        List.of(courseStamp2.getId(), courseStamp1.getId()))
                                .buildUpdateOrders();

                // when
                ResultActions resultActions = getResultActions(token, courseTrip.getId(), request);

                // then
                resultActions
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }

            @Test
            @DisplayName("인증되지 않은 사용자일 경우 401 예외가 발생한다")
            void shouldThrowExceptionWhenUnauthenticated() throws Exception {
                // given
                UpdateStampOrderRequest request = updateStampRequestFixture.buildUpdateOrders();

                // when
                ResultActions resultActions = getResultActions("", courseTrip.getId(), request);

                // then
                resultActions
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(
                                jsonPath("$.status")
                                        .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
            }

            @Test
            @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 예외가 발생한다")
            void shouldThrowExceptionWhenTripIdTypeMismatch() throws Exception {
                // given
                String tripId = "abc";
                UpdateStampOrderRequest request = updateStampRequestFixture.buildUpdateOrders();

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
            @DisplayName("유효하지 않은 여행 ID 라면 404 예외가 발생한다")
            void shouldThrowExceptionWhenInvalidTripId() throws Exception {
                // given
                Long tripId = 10000L;
                UpdateStampOrderRequest request = updateStampRequestFixture.buildUpdateOrders();

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
            @DisplayName("요청한 사용자가 여행의 소유자가 아닐 경우 403 예외가 발생한다")
            void shouldThrowExceptionWhenNotTripOwner() throws Exception {
                // given
                Member newMember = memberTestHelper.saveMember("test@gmail.com", "TEST");
                Trip newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE);
                UpdateStampOrderRequest request = updateStampRequestFixture.buildUpdateOrders();

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
            @DisplayName("삭제된 여행일 경우 400 예외가 발생한다")
            void shouldThrowExceptionWhenAlreadyDeletedTrip() throws Exception {
                // given
                Trip deleted = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE);
                UpdateStampOrderRequest request = updateStampRequestFixture.buildUpdateOrders();

                // when
                ResultActions resultActions = getResultActions(token, deleted.getId(), request);

                // then
                resultActions
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(
                                jsonPath("$.status")
                                        .value(
                                                TripErrorCode.TRIP_ALREADY_DELETED
                                                        .getStatus()
                                                        .value()));
            }

            @Test
            @DisplayName("탐험형 여행이지만 스탬프 순서 변경을 요청한 경우 400 예외가 발생한다")
            void shouldThrowExceptionWhenRequestUpdateStampOrderForExploreTrip() throws Exception {
                // given
                UpdateStampOrderRequest request = updateStampRequestFixture.buildUpdateOrders();

                // when
                ResultActions resultActions = getResultActions(token, exploreTrip.getId(), request);

                // then
                resultActions
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(
                                jsonPath("$.status")
                                        .value(
                                                StampErrorCode
                                                        .CANNOT_UPDATE_ORDER_FOR_EXPLORATION_TRIP
                                                        .getStatus()
                                                        .value()));
            }

            @Test
            @DisplayName("순서 변경을 요청한 ID 리스트에 존재하지 않는 스탬프가 있는 경우 400 예외가 발생한다")
            void shouldThrow400WhenStampIdInUpdateOrderRequestIsInvalid() throws Exception {
                // given
                UpdateStampOrderRequest request =
                        updateStampRequestFixture
                                .withOrderedStampIds(List.of(100L, 200L))
                                .buildUpdateOrders();

                // when
                ResultActions resultActions = getResultActions(token, courseTrip.getId(), request);

                // then
                resultActions
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(
                                jsonPath("$.status")
                                        .value(
                                                StampErrorCode.INVALID_STAMP_ID_IN_REQUEST
                                                        .getStatus()
                                                        .value()));
            }

            @Test
            @DisplayName("요청한 여행에 속한 스탬프가 아닌 경우 403 예외가 발생한다")
            void shouldThrowExceptionWhenStampTripMisMatch() throws Exception {
                // given
                Trip newTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE);
                UpdateStampOrderRequest request =
                        updateStampRequestFixture
                                .withOrderedStampIds(
                                        List.of(courseStamp1.getId(), courseStamp2.getId()))
                                .buildUpdateOrders();

                // when
                ResultActions resultActions = getResultActions(token, newTrip.getId(), request);

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
            @DisplayName("삭제된 스탬프일 경우 400 예외가 발생한다")
            void shouldThrowExceptionWhenAlreadyDeletedStamp() throws Exception {
                // given
                Stamp newStamp = stampTestHelper.saveDeletedStamp(courseTrip, NEXT_STAMP_ORDER);
                UpdateStampOrderRequest request =
                        updateStampRequestFixture
                                .withOrderedStampIds(
                                        List.of(
                                                newStamp.getId(),
                                                courseStamp2.getId(),
                                                courseStamp1.getId()))
                                .buildUpdateOrders();

                // when
                ResultActions resultActions = getResultActions(token, courseTrip.getId(), request);

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
        }
    }

    @Nested
    @DisplayName("스탬프 삭제 API")
    class DeleteStamp {
        private ResultActions getResultActions(String token, Object tripId, Object stampId)
                throws Exception {
            return mockMvc.perform(
                    delete("/api/trips/{tripId}/stamps/{stampId}", tripId, stampId)
                            .header(HttpHeaders.AUTHORIZATION, TokenFixture.TOKEN_PREFIX + token));
        }

        @Test
        @DisplayName("여행의 특정 스탬프를 삭제하고, 여행의 총 스탬프 수가 감소한다")
        void shouldDeleteStamp() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), courseStamp1.getId());

            // then
            resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 예외가 발생한다")
        void shouldThrowExceptionWhenUnauthenticated() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions("", courseTrip.getId(), courseStamp1.getId());

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenTripIdTypeMismatch() throws Exception {
            // given
            String tripId = "abc";

            // when
            ResultActions resultActions = getResultActions(token, tripId, courseStamp1.getId());

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
        @DisplayName("PathVariable 스탬프 ID 타입이 올바르지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenStampIdTypeMismatch() throws Exception {
            // given
            String stampId = "abc";

            // when
            ResultActions resultActions = getResultActions(token, courseTrip.getId(), stampId);

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
        @DisplayName("유효하지 않은 여행 ID 라면 404 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidTripId() throws Exception {
            // given
            Long tripId = 10000L;

            // when
            ResultActions resultActions = getResultActions(token, tripId, courseStamp1.getId());

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("요청한 사용자가 여행의 소유자가 아닐 경우 403 예외가 발생한다")
        void shouldThrowExceptionWhenNotTripOwner() throws Exception {
            // given
            Member newMember = memberTestHelper.saveMember("test@gmail.com", "TEST");
            Trip newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE);

            // when
            ResultActions resultActions =
                    getResultActions(token, newTrip.getId(), courseStamp1.getId());

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
        }

        @Test
        @DisplayName("삭제된 여행일 경우 400 예외가 발생한다")
        void shouldThrowExceptionWhenAlreadyDeletedTrip() throws Exception {
            // given
            Trip deleted = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE);

            // when
            ResultActions resultActions =
                    getResultActions(token, deleted.getId(), courseStamp1.getId());

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_ALREADY_DELETED.getStatus().value()));
        }

        @Test
        @DisplayName("유효하지 않은 스탬프 ID 라면 404 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidStampId() throws Exception {
            // given
            Long stampId = 10000L;

            // when
            ResultActions resultActions = getResultActions(token, courseTrip.getId(), stampId);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(StampErrorCode.STAMP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("요청한 여행에 속한 스탬프가 아닌 경우 403 예외가 발생한다")
        void shouldThrowExceptionWhenStampTripMisMatch() throws Exception {
            // given
            Trip newTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE);

            // when
            ResultActions resultActions =
                    getResultActions(token, newTrip.getId(), courseStamp1.getId());

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
    }

    @Nested
    @DisplayName("스탬프 목록 조회 API")
    class ListStamps {
        private ResultActions getResultActions(String token, Object tripId) throws Exception {
            return mockMvc.perform(
                    get("/api/trips/{tripId}/stamps", tripId)
                            .header(HttpHeaders.AUTHORIZATION, TokenFixture.TOKEN_PREFIX + token));
        }

        @Test
        @DisplayName("여행 ID로 여행에 속한 스탬프 목록을 조회하고 반환한다")
        void shouldGetStampsByTripIdReturnStamps() throws Exception {
            // when
            ResultActions resultActions = getResultActions(token, courseTrip.getId());

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNotEmpty());
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 예외가 발생한다")
        void shouldThrowExceptionWhenUnauthenticated() throws Exception {
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
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenTripIdTypeMismatch() throws Exception {
            // given
            String tripId = "abc";

            // when
            ResultActions resultActions = getResultActions(token, tripId);

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
        @DisplayName("유효하지 않은 여행 ID 라면 404 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidTripId() throws Exception {
            // given
            Long tripId = 10000L;

            // when
            ResultActions resultActions = getResultActions(token, tripId);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("요청한 사용자가 여행의 소유자가 아닐 경우 403 예외가 발생한다")
        void shouldThrowExceptionWhenNotTripOwner() throws Exception {
            // given
            Member newMember = memberTestHelper.saveMember("test@gmail.com", "TEST");
            Trip newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE);

            // when
            ResultActions resultActions = getResultActions(token, newTrip.getId());

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
        }

        @Test
        @DisplayName("삭제된 여행일 경우 400 예외가 발생한다")
        void shouldThrowExceptionWhenAlreadyDeletedTrip() throws Exception {
            // given
            Trip deleted = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE);

            // when
            ResultActions resultActions = getResultActions(token, deleted.getId());

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
    @DisplayName("스탬프 상세 조회 API")
    class GetStamp {
        private ResultActions getResultActions(String token, Object tripId, Object stampId)
                throws Exception {
            return mockMvc.perform(
                    get("/api/trips/{tripId}/stamps/{stampId}", tripId, stampId)
                            .header(HttpHeaders.AUTHORIZATION, TokenFixture.TOKEN_PREFIX + token));
        }

        @Test
        @DisplayName("특정 스탬프 ID로 조회하고 스탬프 정보를 반환한다")
        void shouldGetStampReturnStampInfo() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), courseStamp1.getId());

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNotEmpty());
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 예외가 발생한다")
        void shouldThrowExceptionWhenUnauthenticated() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions("", courseTrip.getId(), courseStamp1.getId());

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenTripIdTypeMismatch() throws Exception {
            // given
            String tripId = "abc";

            // when
            ResultActions resultActions = getResultActions(token, tripId, courseStamp1.getId());

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
        @DisplayName("PathVariable 스탬프 ID 타입이 올바르지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenStampIdTypeMismatch() throws Exception {
            // given
            String stampId = "abc";

            // when
            ResultActions resultActions = getResultActions(token, courseTrip.getId(), stampId);

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
        @DisplayName("유효하지 않은 여행 ID 라면 404 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidTripId() throws Exception {
            // given
            Long tripId = 10000L;

            // when
            ResultActions resultActions = getResultActions(token, tripId, courseStamp1.getId());

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("요청한 사용자가 여행의 소유자가 아닐 경우 403 예외가 발생한다")
        void shouldThrowExceptionWhenNotTripOwner() throws Exception {
            // given
            Member newMember = memberTestHelper.saveMember("test@gmail.com", "TEST");
            Trip newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE);

            // when
            ResultActions resultActions =
                    getResultActions(token, newTrip.getId(), courseStamp1.getId());

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
        }

        @Test
        @DisplayName("삭제된 여행일 경우 400 예외가 발생한다")
        void shouldThrowExceptionWhenAlreadyDeletedTrip() throws Exception {
            // given
            Trip deletedTrip = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE);

            // when
            ResultActions resultActions =
                    getResultActions(token, deletedTrip.getId(), courseStamp1.getId());

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(TripErrorCode.TRIP_ALREADY_DELETED.getStatus().value()));
        }

        @Test
        @DisplayName("유효하지 않은 스탬프 ID 라면 404 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidStampId() throws Exception {
            // given
            Long stampId = 10000L;

            // when
            ResultActions resultActions = getResultActions(token, courseTrip.getId(), stampId);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(StampErrorCode.STAMP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("요청한 스탬프가 해당 여행에 속해있지 않을 경우 403 예외가 발생한다")
        void shouldThrowExceptionWhenStampTripMisMatch() throws Exception {
            // given
            Trip newTrip = tripTestHelper.saveTrip(member, TripCategory.COURSE);

            // when
            ResultActions resultActions =
                    getResultActions(token, newTrip.getId(), courseStamp1.getId());

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
        @DisplayName("삭제된 스탬프일 경우 400 예외가 발생한다")
        void shouldThrowExceptionWhenAlreadyDeletedStamp() throws Exception {
            // given
            Stamp deletedStamp = stampTestHelper.saveDeletedStamp(exploreTrip, 0);

            // when
            ResultActions resultActions =
                    getResultActions(token, exploreTrip.getId(), deletedStamp.getId());

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
    }

    @Nested
    @DisplayName("스탬프 완료 API")
    class CompleteStamp {
        private ResultActions getResultActions(String token, Object tripId, Object stampId)
                throws Exception {
            return mockMvc.perform(
                    patch("/api/trips/{tripId}/stamps/{stampId}/complete", tripId, stampId)
                            .header(HttpHeaders.AUTHORIZATION, TokenFixture.TOKEN_PREFIX + token));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions("", courseTrip.getId(), courseStamp1.getId());

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
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTripIdTypeMismatch() throws Exception {
            // given
            String invalidTripId = "abc";

            // when
            ResultActions resultActions =
                    getResultActions(token, invalidTripId, courseStamp1.getId());

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(
                                            CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH
                                                    .getMessage()));
        }

        @Test
        @DisplayName("PathVariable 스탬프 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenStampIdTypeMismatch() throws Exception {
            // given
            String invalidStampId = "def";

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), invalidStampId);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(
                                            CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH
                                                    .getMessage()));
        }

        @Test
        @DisplayName("삭제된 여행일 경우 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTripAlreadyDeleted() throws Exception {
            // given
            courseTrip.updateDeletedAt();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), courseStamp1.getId());

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
        @DisplayName("여행의 소유자가 아니라면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenNotTripOwner() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(newToken, courseTrip.getId(), courseStamp1.getId());

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
        @DisplayName("스탬프가 요청한 여행에 속하지 않으면 403 Forbidden을 반환한다.")
        void shouldReturnForbiddenWhenStampNotBelongToTrip() throws Exception {
            // when
            ResultActions resultActions =
                    getResultActions(token, exploreTrip.getId(), courseStamp1.getId());

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_NOT_BELONG_TO_TRIP
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(StampErrorCode.STAMP_NOT_BELONG_TO_TRIP.getMessage()));
        }

        @Test
        @DisplayName("유효하지 않은 여행 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenTripIdIsInvalid() throws Exception {
            // given
            Long invalidTripId = 10000L;

            // when
            ResultActions resultActions =
                    getResultActions(token, invalidTripId, courseStamp1.getId());

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
        @DisplayName("유효하지 않은 스탬프 ID가 들어오면 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenStampIdIsInvalid() throws Exception {
            // given
            Long invalidStampId = 10000L;

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), invalidStampId);

            // when & then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(StampErrorCode.STAMP_NOT_FOUND.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(StampErrorCode.STAMP_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("특정 스탬프 하위의 미션이 하나라도 완료되지 않았다면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenAnyMissionIsNotCompleted() throws Exception {
            // given
            courseMission1.updateCompleted();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), courseStamp1.getId());

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            MissionErrorCode.ALL_MISSIONS_NOT_COMPLETED
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(
                                            MissionErrorCode.ALL_MISSIONS_NOT_COMPLETED
                                                    .getMessage()));
        }

        @Test
        @DisplayName("스탬프가 이미 완료되었다면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenStampAlreadyCompleted() throws Exception {
            // given
            courseMission1.updateCompleted();
            courseMission2.updateCompleted();
            courseStamp1.updateCompleted();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), courseStamp1.getId());

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.STAMP_ALREADY_COMPLETED
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(StampErrorCode.STAMP_ALREADY_COMPLETED.getMessage()));
        }

        @Test
        @DisplayName("특정 스탬프 하위의 모든 미션이 완료되었다면 스탬프를 완료합니다.")
        void shouldCompleteStamp() throws Exception {
            // given
            courseMission1.updateCompleted();
            courseMission2.updateCompleted();

            // when
            ResultActions resultActions =
                    getResultActions(token, courseTrip.getId(), courseStamp2.getId());

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }
    }
}
