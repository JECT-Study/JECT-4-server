package com.ject.studytrip.trip.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.trip.domain.error.DailyGoalErrorCode;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.domain.repository.DailyGoalRepository;
import com.ject.studytrip.trip.fixture.DailyGoalFixture;
import com.ject.studytrip.trip.fixture.TripFixture;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DailyGoalQueryService 단위 테스트")
class DailyGoalQueryServiceTest extends BaseUnitTest {
    @InjectMocks private DailyGoalQueryService dailyGoalQueryService;
    @Mock private DailyGoalRepository dailyGoalRepository;

    private Member member;
    private Trip trip;
    private DailyGoal dailyGoal1;
    private DailyGoal dailyGoal2;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMemberFromKakaoWithId(1L);
        trip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
        dailyGoal1 = DailyGoalFixture.createDailyGoalWithId(1L, trip);
        dailyGoal2 = DailyGoalFixture.createDailyGoalWithId(2L, trip);
    }

    @Nested
    @DisplayName("getDailyGoal 메서드는")
    class GetDailyGoal {

        @Test
        @DisplayName("ID로 조회된 데일리 목표가 trip에 속하고 삭제되지 않았다면 반환한다")
        void shouldReturnValidDailyGoal() {
            // given
            Long dailyGoalId = dailyGoal1.getId();
            given(dailyGoalRepository.findById(dailyGoalId)).willReturn(Optional.of(dailyGoal1));

            // when
            DailyGoal result = dailyGoalQueryService.getValidDailyGoal(trip.getId(), dailyGoalId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(dailyGoalId);
        }

        @Test
        @DisplayName("데일리 목표가 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenDailyGoalNotFound() {
            // given
            Long invalidId = -1L;
            given(dailyGoalRepository.findById(invalidId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                            () -> dailyGoalQueryService.getValidDailyGoal(trip.getId(), invalidId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("다른 여행에 속한 데일리 목표일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenNotBelongToTrip() {
            // given
            Long dailyGoalId = dailyGoal1.getId();
            Trip otherTrip = TripFixture.createTripWithId(999L, member, TripCategory.COURSE);
            given(dailyGoalRepository.findById(dailyGoalId)).willReturn(Optional.of(dailyGoal1));

            // when & then
            assertThatThrownBy(
                            () ->
                                    dailyGoalQueryService.getValidDailyGoal(
                                            otherTrip.getId(), dailyGoalId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DailyGoalErrorCode.DAILY_GOAL_NOT_BELONG_TO_TRIP.getMessage());
        }

        @Test
        @DisplayName("삭제된 데일리 목표일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenDeletedDailyGoal() {
            // given
            dailyGoal1.updateDeletedAt();
            Long dailyGoalId = dailyGoal1.getId();
            given(dailyGoalRepository.findById(dailyGoalId)).willReturn(Optional.of(dailyGoal1));

            // when & then
            assertThatThrownBy(
                            () ->
                                    dailyGoalQueryService.getValidDailyGoal(
                                            trip.getId(), dailyGoalId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED.getMessage());
        }
    }

    @Nested
    @DisplayName("getCompleteDailyGoalsByTrip 메서드는")
    class GetCompleteDailyGoalsByTrip {

        @Test
        @DisplayName("데일리 목표가 완료되지 않았다면 빈 목록을 반환한다.")
        void shouldReturnEmptyListWhenDailyGoalsNotCompleted() {
            // given
            Long tripId = trip.getId();
            given(dailyGoalRepository.findAllByTripIdAndCompletedIsTrue(tripId))
                    .willReturn(List.of());

            // when
            List<DailyGoal> result = dailyGoalQueryService.getCompleteDailyGoalsByTripId(tripId);

            // then
            assertThat(result.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("데일리 목표가 완료되었다면 완료된 데일리 목표 목록을 반환한다.")
        void shouldReturnCompleteDailyGoalsWhenDailyGoalsCompleted() {
            // given
            dailyGoal1.updateCompleted();
            dailyGoal2.updateCompleted();
            Long tripId = trip.getId();
            given(dailyGoalRepository.findAllByTripIdAndCompletedIsTrue(tripId))
                    .willReturn(List.of(dailyGoal1, dailyGoal2));

            // when
            List<DailyGoal> result = dailyGoalQueryService.getCompleteDailyGoalsByTripId(tripId);

            // then
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.get(0).getId()).isEqualTo(dailyGoal1.getId());
            assertThat(result.get(1).getId()).isEqualTo(dailyGoal2.getId());
        }
    }
}
