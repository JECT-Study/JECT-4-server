package com.ject.studytrip.trip.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.trip.domain.error.TripErrorCode;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.domain.repository.TripQueryRepository;
import com.ject.studytrip.trip.domain.repository.TripRepository;
import com.ject.studytrip.trip.fixture.CreateTripRequestFixture;
import com.ject.studytrip.trip.fixture.TripFixture;
import com.ject.studytrip.trip.fixture.UpdateTripRequestFixture;
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest;
import com.ject.studytrip.trip.presentation.dto.request.UpdateTripRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@DisplayName("TripService 단위 테스트")
public class TripServiceTest extends BaseUnitTest {
    private static final String TRIP_NAME = "TRIP NAME UPDATED";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 5;

    @InjectMocks private TripService tripService;
    @Mock private TripRepository tripRepository;
    @Mock private TripQueryRepository tripQueryRepository;

    private Member member;
    private Trip trip;

    @BeforeEach
    void setup() {
        member = MemberFixture.createMemberFromKakaoWithId(1L);
        trip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
    }

    @Nested
    @DisplayName("여행을 생성한다")
    class CreateTrip {

        @Test
        @DisplayName("여행 정보를 생성해 DB에 저장하고, 저장된 Trip을 반환한다")
        void shouldSaveTripReturnTrip() {
            // given
            CreateTripRequest request = new CreateTripRequestFixture().build();
            given(tripRepository.save(any())).willReturn(trip);

            // when
            Trip saved = tripService.createTrip(member, request);

            // then
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isEqualTo(trip.getId());
        }

        @Test
        @DisplayName("여행 카테고리가 누락되었을 경우 예외가 발생한다")
        void shouldThrowExceptionWhenMissingTripCategory() {
            // given
            CreateTripRequest request = new CreateTripRequestFixture().withCategory(null).build();

            // when & then
            assertThatThrownBy(() -> tripService.createTrip(member, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(TripErrorCode.TRIP_CATEGORY_REQUIRED.getMessage());
        }

        @Test
        @DisplayName("여행 카테고리가 유효하지 않은 경우 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidTripCategory() {
            // given
            CreateTripRequest request = new CreateTripRequestFixture().withCategory("test").build();

            // when & then
            assertThatThrownBy(() -> tripService.createTrip(member, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(TripErrorCode.INVALID_TRIP_CATEGORY.getMessage());
        }

        @Test
        @DisplayName("코스형 여행인데 종료일이 null일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenCourseTripEndDateIsNull() {
            // given
            CreateTripRequest request =
                    new CreateTripRequestFixture()
                            .withCategory(TripCategory.COURSE.name())
                            .withEndDate(null)
                            .build();

            // When & Then
            assertThatThrownBy(() -> tripService.createTrip(member, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(TripErrorCode.COURSE_TRIP_END_DATE_REQUIRED.getMessage());
        }

        @Test
        @DisplayName("여행 종료일이 시작일보다 이전일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenEndDateIsBeforeStartDate() {
            // given
            CreateTripRequest request =
                    new CreateTripRequestFixture()
                            .withEndDate(LocalDate.now().minusDays(7))
                            .build();

            // When & Then
            assertThatThrownBy(() -> tripService.createTrip(member, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(
                            TripErrorCode.TRIP_END_DATE_BEFORE_START_DATE.getMessage());
        }

        @Test
        @DisplayName("함께 등록할 여행 스탬프 목록이 비어있으면 예외가 발생한다")
        void shouldThrowExceptionWhenStampsIsEmpty() {
            // given
            CreateTripRequest request =
                    new CreateTripRequestFixture().withStamps(List.of()).build();

            // When & Then
            assertThatThrownBy(() -> tripService.createTrip(member, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(TripErrorCode.TRIP_STAMP_REQUIRED.getMessage());
        }
    }

    @Nested
    @DisplayName("여행을 수정한다")
    class UpdateTrip {

        @Test
        @DisplayName("특정 여행의 정보를 수정하고 DB에 반영한다")
        void shouldUpdateTrip() {
            // given
            UpdateTripRequest request = new UpdateTripRequestFixture().withName(TRIP_NAME).build();

            // When
            tripService.updateTrip(member.getId(), trip, request);

            // Then
            assertThat(trip.getName()).isEqualTo(TRIP_NAME);
        }

        @Test
        @DisplayName("여행 종료일이 시작일보다 이전일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenEndDateIsBeforeStartDate() {
            // given
            UpdateTripRequest request =
                    new UpdateTripRequestFixture()
                            .withEndDate(trip.getStartDate().minusDays(1))
                            .build();

            // When & Then
            assertThatThrownBy(() -> tripService.updateTrip(member.getId(), trip, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(
                            TripErrorCode.TRIP_END_DATE_BEFORE_START_DATE.getMessage());
        }

        @Test
        @DisplayName("여행의 총 스탬프 수를 +1 증가시킨다")
        void shouldIncreaseTotalStamps() {
            // given
            int tripTotalStamps = trip.getTotalStamps();

            // when
            trip.increaseTotalStamps();

            // then
            assertThat(trip.getTotalStamps()).isEqualTo(tripTotalStamps + 1);
        }

        @Test
        @DisplayName("여행의 총 스탬프 수를 -1 감소시킨다")
        void shouldDecreaseTotalStamps() {
            // given
            int tripTotalStamps = trip.getTotalStamps();

            // when
            trip.decreaseTotalStamps();

            // then
            assertThat(trip.getTotalStamps()).isEqualTo(tripTotalStamps - 1);
        }
    }

    @Nested
    @DisplayName("특정 여행을 삭제한다")
    class DeleteTrip {

        @Test
        @DisplayName("특정 여행을 deletedAt 필드를 현재 시간으로 업데이트한다")
        void shouldDeleteTripForUpdateDeletedAt() {
            // when
            tripService.deleteTrip(member.getId(), trip);

            // then
            assertThat(trip.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("특정 여행의 정보를 조회한다")
    class GetTrip {

        @Test
        @DisplayName("특정 여행 ID로 DB에서 조회한 후 반환한다")
        void shouldGetTripByTripIdReturnTrip() {
            // given
            given(tripRepository.findById(trip.getId())).willReturn(Optional.of(trip));

            // when
            Trip result = tripService.getTrip(trip.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(trip.getId());
        }

        @Test
        @DisplayName("특정 여행 ID로 DB에서 조회한 후 유효한 여행을 반환한다")
        void shouldGetTripByTripIdReturnValidTrip() {
            // given
            given(tripRepository.findById(trip.getId())).willReturn(Optional.of(trip));

            // when
            Trip result = tripService.getValidTrip(member.getId(), trip.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(trip.getId());
        }

        @Test
        @DisplayName("여행의 소유자가 아닐 경우 예외가 발생한다")
        void shouldThrowExceptionWhenNotTripOwner() {
            // given
            Member newMember = MemberFixture.createMemberFromKakao();
            given(tripRepository.findById(trip.getId())).willReturn(Optional.of(trip));

            // When & Then
            assertThatThrownBy(() -> tripService.getValidTrip(newMember.getId(), trip.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(TripErrorCode.NOT_TRIP_OWNER.getMessage());
        }

        @Test
        @DisplayName("이미 삭제된 여행일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenAlreadyTrip() {
            // given
            Trip deleted = TripFixture.createDeletedTrip(member);
            given(tripRepository.findById(any())).willReturn(Optional.of(deleted));

            // when & then
            assertThatThrownBy(() -> tripService.getValidTrip(member.getId(), deleted.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TripErrorCode.TRIP_ALREADY_DELETED.getMessage());
        }
    }

    @Nested
    @DisplayName("여행 목록을 조회한다")
    class ListTrips {

        @Test
        @DisplayName("로그인된 사용자의 여행 목록을 DB에서 조회하고 슬라이스 처리해 반환한다")
        void shouldGetTripsReturnSlicePaged() {
            // given
            List<Trip> trips = List.of(trip);
            Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
            Slice<Trip> results = new SliceImpl<>(trips, pageable, false);
            given(tripQueryRepository.findSliceByMemberId(member.getId(), pageable))
                    .willReturn(results);

            // when
            Slice<Trip> sliceTrips =
                    tripService.getTripsSliceByMemberId(member.getId(), DEFAULT_PAGE, DEFAULT_SIZE);

            // then
            assertThat(sliceTrips.hasContent()).isTrue();
            assertThat(sliceTrips.hasNext()).isFalse();
        }
    }
}
