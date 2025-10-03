package com.ject.studytrip.trip.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.trip.application.dto.TripCountInfo;
import com.ject.studytrip.trip.domain.error.TripErrorCode;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.domain.repository.TripQueryRepository;
import com.ject.studytrip.trip.domain.repository.TripRepository;
import com.ject.studytrip.trip.fixture.TripFixture;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
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

@DisplayName("TripQueryService 단위 테스트")
class TripQueryServiceTest extends BaseUnitTest {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 5;

    @InjectMocks private TripQueryService tripQueryService;
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
    @DisplayName("getTrip 메서드는")
    class GetTrip {

        @Test
        @DisplayName("존재하지 않는 여행 ID로 조회하면 예외가 발생한다.")
        void shouldThrowExceptionWhenTripIdNotFound() {
            // given
            Long invalidId = -1L;
            given(tripRepository.findById(invalidId)).willReturn(Optional.empty());

            // when & then
            Assertions.assertThatThrownBy(() -> tripQueryService.getTrip(invalidId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TripErrorCode.TRIP_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("특정 여행 ID로 DB에서 조회한 후 반환한다")
        void shouldGetTripByTripIdReturnTrip() {
            // given
            Long tripId = trip.getId();
            given(tripRepository.findById(tripId)).willReturn(Optional.of(trip));

            // when
            Trip result = tripQueryService.getTrip(tripId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(tripId);
        }
    }

    @Nested
    @DisplayName("getValidTrip 메서드는")
    class GetValidTrip {

        @Test
        @DisplayName("여행의 소유자가 아닐 경우 예외가 발생한다")
        void shouldThrowExceptionWhenNotTripOwner() {
            // given
            Member newMember = MemberFixture.createMemberFromKakao();
            Long tripId = trip.getId();
            given(tripRepository.findById(tripId)).willReturn(Optional.of(trip));

            // When & Then
            assertThatThrownBy(() -> tripQueryService.getValidTrip(newMember.getId(), tripId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(TripErrorCode.NOT_TRIP_OWNER.getMessage());
        }

        @Test
        @DisplayName("이미 삭제된 여행일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenAlreadyTrip() {
            // given
            trip.updateDeletedAt();
            Long deletedId = trip.getId();
            given(tripRepository.findById(deletedId)).willReturn(Optional.of(trip));

            // when & then
            assertThatThrownBy(() -> tripQueryService.getValidTrip(member.getId(), deletedId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TripErrorCode.TRIP_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("특정 여행 ID로 DB에서 조회한 후 유효한 여행을 반환한다")
        void shouldGetTripByTripIdReturnValidTrip() {
            // given
            Long tripId = trip.getId();
            given(tripRepository.findById(tripId)).willReturn(Optional.of(trip));

            // when
            Trip result = tripQueryService.getValidTrip(member.getId(), trip.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(tripId);
        }
    }

    @Nested
    @DisplayName("getTripsSliceByMemberId 메서드는")
    class GetTripsSliceByMemberId {

        @Test
        @DisplayName("로그인된 사용자의 여행 목록을 DB에서 조회하고 슬라이스 처리해 반환한다")
        void shouldGetTripsReturnSlicePaged() {
            // given
            Long memberId = member.getId();
            List<Trip> trips = List.of(trip);
            Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
            Slice<Trip> results = new SliceImpl<>(trips, pageable, false);
            given(tripQueryRepository.findSliceByMemberId(memberId, pageable)).willReturn(results);

            // when
            Slice<Trip> sliceTrips =
                    tripQueryService.getTripsSliceByMemberId(memberId, DEFAULT_PAGE, DEFAULT_SIZE);

            // then
            assertThat(sliceTrips.hasContent()).isTrue();
            assertThat(sliceTrips.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("getActiveTripCountsByMemberId 메서드는")
    class GetActiveTripCountsByMemberId {

        @Test
        @DisplayName("해당 멤버의 여행이 존재하지 않으면 0을 반환한다.")
        void shouldReturnZeroWhenTripDoesNotExistForMember() {
            // given
            Long memberId = member.getId();
            given(
                            tripQueryRepository.countActiveTripsByMemberIdAndCategory(
                                    memberId, TripCategory.COURSE))
                    .willReturn(0L);
            given(
                            tripQueryRepository.countActiveTripsByMemberIdAndCategory(
                                    memberId, TripCategory.EXPLORE))
                    .willReturn(0L);

            // when
            TripCountInfo result = tripQueryService.getActiveTripCountsByMemberId(memberId);

            // then
            assertThat(result.course()).isZero();
            assertThat(result.explore()).isZero();
        }

        @Test
        @DisplayName("코스형과 탐험형 여행 개수를 각각 조회하여 TripCount를 반환한다.")
        void shouldReturnTripCountByCategory() {
            // given
            Long memberId = member.getId();
            given(
                            tripQueryRepository.countActiveTripsByMemberIdAndCategory(
                                    memberId, TripCategory.COURSE))
                    .willReturn(3L);
            given(
                            tripQueryRepository.countActiveTripsByMemberIdAndCategory(
                                    memberId, TripCategory.EXPLORE))
                    .willReturn(2L);

            // when
            TripCountInfo result = tripQueryService.getActiveTripCountsByMemberId(memberId);

            // then
            assertThat(result.course()).isEqualTo(3L);
            assertThat(result.explore()).isEqualTo(2L);
        }
    }
}
