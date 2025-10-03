package com.ject.studytrip.trip.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("TripCommandService 단위 테스트")
class TripCommandServiceTest extends BaseUnitTest {
    private static final String NEW_TRIP_NAME = "NEW TRIP NAME";

    @InjectMocks private TripCommandService tripCommandService;
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
    @DisplayName("createTrip 메서드는")
    class CreateTrip {

        @Test
        @DisplayName("여행 정보를 생성해 DB에 저장하고, 저장된 Trip을 반환한다")
        void shouldSaveTripReturnTrip() {
            // given
            CreateTripRequest request = new CreateTripRequestFixture().build();
            given(tripRepository.save(any())).willReturn(trip);

            // when
            Trip saved = tripCommandService.createTrip(member, request);

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
            assertThatThrownBy(() -> tripCommandService.createTrip(member, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(TripErrorCode.TRIP_CATEGORY_REQUIRED.getMessage());
        }

        @Test
        @DisplayName("여행 카테고리가 유효하지 않은 경우 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidTripCategory() {
            // given
            CreateTripRequest request = new CreateTripRequestFixture().withCategory("test").build();

            // when & then
            assertThatThrownBy(() -> tripCommandService.createTrip(member, request))
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
            assertThatThrownBy(() -> tripCommandService.createTrip(member, request))
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
            assertThatThrownBy(() -> tripCommandService.createTrip(member, request))
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
            assertThatThrownBy(() -> tripCommandService.createTrip(member, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(TripErrorCode.TRIP_STAMP_REQUIRED.getMessage());
        }
    }

    @Nested
    @DisplayName("updateTrip 메서드는")
    class UpdateTrip {

        @Test
        @DisplayName("특정 여행의 정보를 수정하고 DB에 반영한다")
        void shouldUpdateTrip() {
            // given
            UpdateTripRequest request =
                    new UpdateTripRequestFixture().withName(NEW_TRIP_NAME).build();

            // When
            tripCommandService.updateTrip(trip, request);

            // Then
            assertThat(trip.getName()).isEqualTo(NEW_TRIP_NAME);
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
            assertThatThrownBy(() -> tripCommandService.updateTrip(trip, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(
                            TripErrorCode.TRIP_END_DATE_BEFORE_START_DATE.getMessage());
        }
    }

    @Nested
    @DisplayName("increaseTotalStamps 메서드는")
    class IncreaseTotalStamps {

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
    }

    @Nested
    @DisplayName("decreaseTotalStamps 메서드는")
    class DecreaseTotalStamps {

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
    @DisplayName("deleteTrip 메서드는")
    class DeleteTrip {

        @Test
        @DisplayName("특정 여행을 deletedAt 필드를 현재 시간으로 업데이트한다")
        void shouldDeleteTripForUpdateDeletedAt() {
            // when
            tripCommandService.deleteTrip(trip);

            // then
            assertThat(trip.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("completeTrip 메서드는")
    class CompleteTrip {

        @Test
        @DisplayName("이미 완료된 여행이면 예외가 발생한다.")
        void shouldThrowExceptionWhenTripIsAlreadyCompleted() {
            // given
            trip.updateCompleted();

            // when & then
            assertThatThrownBy(() -> tripCommandService.completeTrip(trip))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TripErrorCode.TRIP_ALREADY_COMPLETED.getMessage());
        }

        @Test
        @DisplayName("유효한 여행이 들어오면, completed 필드를 true로 업데이트한다.")
        void shouldCompleteStamp() {
            // when
            tripCommandService.completeTrip(trip);

            // then
            assertThat(trip.isCompleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("increaseCompletedStamps 메서드는")
    class IncreaseCompletedStamps {

        @Test
        @DisplayName("유효한 여행이 들어오면, Trip의 completedStamps 필드를 1 증가시킨다.")
        void shouldIncreaseCompletedStamps() {
            // when
            tripCommandService.increaseCompletedStamps(trip);

            // then
            assertThat(trip.getCompletedStamps()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("hardDeleteTrips 메서드는")
    class HardDeleteTrips {

        @Test
        @DisplayName("삭제된 여행이 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDeletedTripsDoNotExist() {
            // given
            given(tripQueryRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L);

            // when
            long result = tripCommandService.hardDeleteTrips();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 여행이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDeletedTripsExist() {
            // given
            given(tripQueryRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L);

            // when
            long result = tripCommandService.hardDeleteTrips();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteTripsOwnedByDeletedMember 메서드는")
    class HardDeleteTripsOwnedByDeletedMember {

        @Test
        @DisplayName("삭제된 멤버가 소유한 여행이 없으면 0을 반환한다.")
        void shouldReturnZeroWhenTripsOwnedByDeletedMemberDoNotExist() {
            // given
            given(tripQueryRepository.deleteAllByDeletedMemberOwner()).willReturn(0L);

            // when
            long result = tripCommandService.hardDeleteTripsOwnedByDeletedMember();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 멤버가 소유한 여행이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenTripsOwnedByDeletedMemberExist() {
            // given
            given(tripQueryRepository.deleteAllByDeletedMemberOwner()).willReturn(5L);

            // when
            long result = tripCommandService.hardDeleteTripsOwnedByDeletedMember();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }
}
