package com.ject.studytrip.trip.presentation.controller;

import static com.ject.studytrip.auth.fixture.TokenFixture.TOKEN_PREFIX;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ject.studytrip.BaseIntegrationTest;
import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.fixture.TokenFixture;
import com.ject.studytrip.auth.helper.TokenTestHelper;
import com.ject.studytrip.global.common.response.StandardResponse;
import com.ject.studytrip.global.exception.error.CommonErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.helper.MemberTestHelper;
import com.ject.studytrip.stamp.domain.error.StampErrorCode;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.helper.StampTestHelper;
import com.ject.studytrip.trip.domain.error.TripErrorCode;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.CreateTripRequestFixture;
import com.ject.studytrip.trip.fixture.UpdateTripRequestFixture;
import com.ject.studytrip.trip.helper.TripTestHelper;
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest;
import com.ject.studytrip.trip.presentation.dto.request.UpdateTripRequest;
import com.ject.studytrip.trip.presentation.dto.response.LoadTripCategoryResponse;
import com.ject.studytrip.trip.presentation.dto.response.LoadTripDetailResponse;
import com.ject.studytrip.trip.presentation.dto.response.LoadTripsSliceResponse;
import java.time.LocalDate;
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

@DisplayName("TripController 통합 테스트")
public class TripControllerIntegrationTest extends BaseIntegrationTest {
    private static final String TRIP_CATEGORY_COURSE = TripCategory.COURSE.name();
    private static final String TRIP_CATEGORY_EXPLORE = TripCategory.EXPLORE.name();

    @Autowired private TokenTestHelper tokenTestHelper;
    @Autowired private MemberTestHelper memberTestHelper;
    @Autowired private TripTestHelper tripTestHelper;
    @Autowired private StampTestHelper stampTestHelper;

    private Member member;
    private Trip trip;
    private String token;
    private String newToken;
    private Stamp stamp1;
    private Stamp stamp2;

    @BeforeEach
    void setup() {
        member = memberTestHelper.saveMember();
        token =
                tokenTestHelper.createAccessToken(
                        member.getId().toString(), member.getRole().name());
        trip = tripTestHelper.saveTrip(member, TripCategory.COURSE);
        stamp1 = stampTestHelper.saveStamp(trip, 1);
        stamp2 = stampTestHelper.saveStamp(trip, 2);

        Member newMember = memberTestHelper.saveMember("test@kakao.com", "TEST NICKNAME");
        newToken =
                tokenTestHelper.createAccessToken(
                        newMember.getId().toString(), newMember.getRole().name());
    }

    @Nested
    @DisplayName("여행 카테고리 목록 조회 API")
    class GetTripCategory {

        private ResultActions getResultActions() throws Exception {
            return mockMvc.perform(get("/api/trips/categories"));
        }

        @Test
        @DisplayName("여행 카테고리 종류를 조회한다")
        void shouldGetTripCategories() throws Exception {
            // when
            ResultActions resultActions = getResultActions();

            // then
            resultActions.andExpect(status().isOk());

            StandardResponse response = parseResponse(resultActions, StandardResponse.class);
            Object rawData = response.data();
            List<LoadTripCategoryResponse> categoryResponses =
                    objectMapper.convertValue(rawData, List.class);

            assertThat(categoryResponses.isEmpty()).isFalse();
            assertThat(categoryResponses.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("여행 생성 API")
    class CreateTrip {

        private ResultActions getResultActions(String token, CreateTripRequest request)
                throws Exception {
            return mockMvc.perform(
                    post("/api/trips")
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("유효한 요청으로 여행 정보를 생성한다")
        void shouldCreateTrip() throws Exception {
            // given
            CreateTripRequest request = new CreateTripRequestFixture().build();

            // when
            ResultActions resultActions = getResultActions(token, request);

            // then
            resultActions.andExpect(status().isCreated());
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 예외가 발생한다")
        void shouldThrowExceptionWhenUnauthenticated() throws Exception {
            // given
            CreateTripRequest request = new CreateTripRequestFixture().build();

            // when
            ResultActions resultActions = getResultActions("", request);

            // then
            resultActions.andExpect(status().is(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("여행을 생성하는데 필요한 필수 요청 값이 누락되거나 유효하지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidRequiredFields() throws Exception {
            // given
            CreateTripRequest nonTripNameRequest =
                    new CreateTripRequestFixture()
                            .withName("")
                            .withCategory("test")
                            .withEndDate(null)
                            .build();

            // when
            ResultActions resultActions = getResultActions(token, nonTripNameRequest);

            // then
            resultActions.andExpect(
                    status().is(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.getStatus().value()));
        }

        @Test
        @DisplayName("여행 종료일이 과거일 경우 400 예외가 발생한다")
        void shouldThrowExceptionWhenEndDateIsInThePast() throws Exception {
            // given
            CreateTripRequest invalidDateRequest =
                    new CreateTripRequestFixture()
                            .withEndDate(LocalDate.now().minusDays(10))
                            .build();

            // when
            ResultActions resultActions = getResultActions(token, invalidDateRequest);

            // then
            resultActions.andExpect(
                    status().is(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.getStatus().value()));
        }

        @Test
        @DisplayName("여행의 카테고리가 코스형이고 종료일이 존재하지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenCourseTripHasNoEndDate() throws Exception {
            // given
            CreateTripRequest request =
                    new CreateTripRequestFixture()
                            .withCategory(TRIP_CATEGORY_COURSE)
                            .withEndDate(null)
                            .build();

            // when
            ResultActions resultActions = getResultActions(token, request);

            // then
            resultActions.andExpect(
                    status().is(TripErrorCode.COURSE_TRIP_END_DATE_REQUIRED.getStatus().value()));
        }

        @Test
        @DisplayName("함께 요청한 여행의 스탬프 목록이 없으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenRequestStampsIsEmpty() throws Exception {
            // given
            CreateTripRequest request =
                    new CreateTripRequestFixture().withStamps(List.of()).build();

            // when
            ResultActions resultActions = getResultActions(token, request);

            // when & then
            resultActions.andExpect(
                    status().is(TripErrorCode.TRIP_STAMP_REQUIRED.getStatus().value()));
        }
    }

    @Nested
    @DisplayName("여행 수정 API")
    class UpdateTrip {

        private ResultActions getResultActions(
                String token, Object tripId, UpdateTripRequest request) throws Exception {
            return mockMvc.perform(
                    patch("/api/trips/{tripId}", tripId)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("특정 여행 정보를 수정한다")
        void shouldUpdateTrip() throws Exception {
            // given
            UpdateTripRequest request = new UpdateTripRequestFixture().withName("여행 이름 수정").build();

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), request);

            // then
            resultActions.andExpect(status().isOk());
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 예외가 발생한다")
        void shouldThrowExceptionWhenUnauthenticated() throws Exception {
            // given
            UpdateTripRequest request = new UpdateTripRequestFixture().build();

            // when
            ResultActions resultActions = getResultActions("", trip.getId(), request);

            // then
            resultActions.andExpect(status().is(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenTripIdTypeMismatch() throws Exception {
            // given
            String tripId = "abc";
            UpdateTripRequest request =
                    new UpdateTripRequestFixture()
                            .withEndDate(LocalDate.now().minusDays(7))
                            .build();

            // when
            ResultActions resultActions = getResultActions(token, tripId, request);

            // then
            resultActions.andExpect(
                    status().is(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.getStatus().value()));
        }

        @Test
        @DisplayName("수정 요청한 여행 종료일이 과거일 경우 400 예외가 발생한다")
        void shouldThrowExceptionWhenEndDateIsInThePast() throws Exception {
            // given
            UpdateTripRequest request =
                    new UpdateTripRequestFixture()
                            .withEndDate(LocalDate.now().minusDays(7))
                            .build();

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), request);

            // then
            resultActions.andExpect(
                    status().is(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.getStatus().value()));
        }

        @Test
        @DisplayName("유효하지 않은 여행 ID 라면 404 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidTripId() throws Exception {
            // given
            Long tripId = 10000L;
            UpdateTripRequest request = new UpdateTripRequestFixture().build();

            // when
            ResultActions resultActions = getResultActions(token, tripId, request);

            // when & then
            resultActions.andExpect(status().is(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("수정할 권한이 없으면 403 예외가 발생한다")
        void shouldThrowExceptionWhenUpdatingTripWithoutPermission() throws Exception {
            // given
            Member newMember = memberTestHelper.saveMember("test@gmail.com", "test");
            Trip newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE);
            UpdateTripRequest request = new UpdateTripRequestFixture().build();

            // when
            ResultActions resultActions = getResultActions(token, newTrip.getId(), request);

            // then
            resultActions.andExpect(status().is(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
        }

        @Test
        @DisplayName("수정 요청한 여행 정보가 이미 삭제된 여행이라면 400 예외가 발생한다")
        void shouldThrowExceptionWhenAlreadyDeletedTrip() throws Exception {
            // given
            Trip deletedTrip = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE);
            UpdateTripRequest request = new UpdateTripRequestFixture().build();

            // when
            ResultActions resultActions = getResultActions(token, deletedTrip.getId(), request);

            // then
            resultActions.andExpect(
                    status().is(TripErrorCode.TRIP_ALREADY_DELETED.getStatus().value()));
        }

        @Test
        @DisplayName("종료 날짜가 시작 날짜보다 이전인 경우 400 예외가 발생한다")
        void shouldThrowExceptionWhenUpdatingTripWithInvalidEndDate() throws Exception {
            // given
            UpdateTripRequest request =
                    new UpdateTripRequestFixture()
                            .withEndDate(trip.getStartDate().minusDays(1))
                            .build();

            // when
            ResultActions resultActions = getResultActions(token, trip.getId(), request);

            // when & then
            resultActions.andExpect(
                    status().is(TripErrorCode.TRIP_END_DATE_BEFORE_START_DATE.getStatus().value()));
        }
    }

    @Nested
    @DisplayName("여행 삭제 API")
    class DeleteTrip {

        private ResultActions getResultActions(String token, Object tripId) throws Exception {
            return mockMvc.perform(
                    delete("/api/trips/{tripId}", tripId)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token));
        }

        @Test
        @DisplayName("특정 여행 정보를 삭제한다")
        void shouldDeleteTrip() throws Exception {
            // when
            ResultActions resultActions = getResultActions(token, trip.getId());

            // then
            resultActions.andExpect(status().isOk());
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 예외가 발생한다")
        void shouldThrowExceptionWhenUnauthenticated() throws Exception {
            // when
            ResultActions resultActions = getResultActions("", trip.getId());

            // then
            resultActions.andExpect(status().is(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 타입이 올바르지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenTripIdTypeMismatch() throws Exception {
            // given
            String tripId = "abc";

            // when
            ResultActions resultActions = getResultActions(token, tripId);

            // then
            resultActions.andExpect(
                    status().is(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.getStatus().value()));
        }

        @Test
        @DisplayName("유효하지 않은 여행 ID 라면 404 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidTripId() throws Exception {
            // given
            Long tripId = 10000L;

            // when
            ResultActions resultActions = getResultActions(token, tripId);

            // when & then
            resultActions.andExpect(status().is(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("여행을 삭제할 권한이 없으면 403 예외가 발생한다")
        void shouldThrowExceptionWhenNoPermission() throws Exception {
            // given
            Member newMember = memberTestHelper.saveMember("test@gmail.com", "test");
            Trip newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE);

            // when
            ResultActions resultActions = getResultActions(token, newTrip.getId());

            // when & then
            resultActions.andExpect(status().is(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
        }
    }

    @Nested
    @DisplayName("여행 상세 조회 API")
    class GetTrip {
        private ResultActions getResultActions(String token, Object tripId) throws Exception {
            return mockMvc.perform(
                    get("/api/trips/{tripId}", tripId)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token));
        }

        @Test
        @DisplayName("여행 ID로 특정 여행을 상세 조회한다")
        void shouldLoadTripByTripId() throws Exception {
            // When
            ResultActions resultActions = getResultActions(token, trip.getId());

            // Then
            resultActions.andExpect(status().isOk());

            StandardResponse response = parseResponse(resultActions, StandardResponse.class);
            Object rawData = response.data();
            LoadTripDetailResponse detailResponse =
                    objectMapper.convertValue(rawData, LoadTripDetailResponse.class);

            assertThat(detailResponse.tripId()).isEqualTo(trip.getId());
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 예외가 발생한다")
        void shouldThrowExceptionWhenUnauthenticated() throws Exception {
            // When
            ResultActions resultActions = getResultActions("", trip.getId());

            // Then
            resultActions.andExpect(status().is(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("PathVariable 여행 ID 값의 타입이 올바르지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenTripIdTypeMissMatch() throws Exception {
            // given
            String tripId = "abc";

            // when
            ResultActions resultActions = getResultActions(token, tripId);

            // then
            resultActions.andExpect(
                    status().is(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.getStatus().value()));
        }

        @Test
        @DisplayName("유효하지 않은 여행 ID 라면 404 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidTripId() throws Exception {
            // given
            Long tripId = 10000L;

            // when
            ResultActions resultActions = getResultActions(token, tripId);

            // when & then
            resultActions.andExpect(status().is(TripErrorCode.TRIP_NOT_FOUND.getStatus().value()));
        }

        @Test
        @DisplayName("여행의 소유자가 아닐 경우 403 예외가 발생한다")
        void shouldThrowExceptionWhenNotTripOwner() throws Exception {
            // given
            Member newMember = memberTestHelper.saveMember("test@gmail.com", "test");
            Trip newTrip = tripTestHelper.saveTrip(newMember, TripCategory.COURSE);

            // when
            ResultActions resultActions = getResultActions(token, newTrip.getId());

            // then
            resultActions.andExpect(status().is(TripErrorCode.NOT_TRIP_OWNER.getStatus().value()));
        }

        @Test
        @DisplayName("이미 삭제된 여행일 경우 400 예외가 발생한다")
        void shouldThrowExceptionWhenAlreadyDeleted() throws Exception {
            // given
            Trip deleted = tripTestHelper.saveDeletedTrip(member, TripCategory.COURSE);

            // when
            ResultActions resultActions = getResultActions(token, deleted.getId());

            // when & then
            resultActions.andExpect(
                    status().is(TripErrorCode.TRIP_ALREADY_DELETED.getStatus().value()));
        }
    }

    @Nested
    @DisplayName("여행 목록 조회 API")
    class ListTrips {
        private ResultActions getResultActions(String token, String page, String size)
                throws Exception {
            return mockMvc.perform(
                    get("/api/trips")
                            .param("page", page)
                            .param("size", size)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token));
        }

        @Test
        @DisplayName("로그인한 사용자의 여행 목록을 조회하고 슬라이스 처리한다")
        void shouldLoadTripsWithSlicePaging() throws Exception {
            // Given
            String page = "0";
            String size = "5";

            // When
            ResultActions resultActions = getResultActions(token, page, size);

            // Then
            resultActions.andExpect(status().isOk());

            StandardResponse response = parseResponse(resultActions, StandardResponse.class);
            Object rawData = response.data();
            LoadTripsSliceResponse sliceResponse =
                    objectMapper.convertValue(rawData, LoadTripsSliceResponse.class);

            assertThat(sliceResponse.tripInfos().size()).isEqualTo(1);
            assertThat(sliceResponse.hasNext()).isFalse();
        }

        @Test
        @DisplayName("인증되지 않은 사용자일 경우 401 예외가 발생한다")
        void shouldThrowExceptionWhenUnauthenticated() throws Exception {
            // Given
            String page = "0";
            String size = "5";

            // when
            ResultActions resultActions = getResultActions("", page, size);

            // Then
            resultActions.andExpect(status().is(AuthErrorCode.UNAUTHENTICATED.getStatus().value()));
        }

        @Test
        @DisplayName("페이징 파라미터 타입이 올바르지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenPagingParameterTypeMismatch() throws Exception {
            // Given
            String page = "test";
            String size = "test";

            // when
            ResultActions resultActions = getResultActions(token, page, size);

            // Then
            resultActions.andExpect(
                    status().is(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.getStatus().value()));
        }

        @Test
        @DisplayName("페이징 파라미터가 유효하지 않으면 400 예외가 발생한다")
        void shouldThrowExceptionWhenPagingParameterIsInvalid() throws Exception {
            // Given
            String page = "-1";
            String size = "100";

            // when
            ResultActions resultActions = getResultActions(token, page, size);

            // Then
            resultActions.andExpect(
                    status().is(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.getStatus().value()));
        }
    }

    @Nested
    @DisplayName("여행 완료 API")
    class CompleteTrip {
        private ResultActions getResultActions(String token, Object tripId) throws Exception {
            return mockMvc.perform(
                    patch("/api/trips/{tripId}/complete", tripId)
                            .header(
                                    org.apache.http.HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + token));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // when
            ResultActions resultActions = getResultActions("", trip.getId());

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
            ResultActions resultActions = getResultActions(token, invalidTripId);

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
            trip.updateDeletedAt();

            // when
            ResultActions resultActions = getResultActions(token, trip.getId());

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
            ResultActions resultActions = getResultActions(newToken, trip.getId());

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
            ResultActions resultActions = getResultActions(token, invalidTripId);

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
        @DisplayName("특정 여행 하위의 스탬프가 하나라도 완료되지 않았다면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenAnyStampIsNotCompleted() throws Exception {
            // given

            // when
            ResultActions resultActions = getResultActions(token, trip.getId());

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            StampErrorCode.ALL_STAMPS_NOT_COMPLETED
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(StampErrorCode.ALL_STAMPS_NOT_COMPLETED.getMessage()));
        }

        @Test
        @DisplayName("여행이 이미 완료되었다면 400 Bad Request를 반환한다.")
        void shouldReturnBadRequestWhenTripAlreadyCompleted() throws Exception {
            // given
            stamp1.updateCompleted();
            stamp2.updateCompleted();
            trip.updateCompleted();

            // when
            ResultActions resultActions = getResultActions(token, trip.getId());

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            TripErrorCode.TRIP_ALREADY_COMPLETED
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(TripErrorCode.TRIP_ALREADY_COMPLETED.getMessage()));
        }

        @Test
        @DisplayName("특정 여행 하위의 모든 스탬프가 완료되었다면 여행을 완료합니다.")
        void shouldCompleteTrip() throws Exception {
            // given
            stamp1.updateCompleted();
            stamp2.updateCompleted();

            // when
            ResultActions resultActions = getResultActions(token, trip.getId());

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }
    }
}
