package com.ject.studytrip.trip.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.domain.repository.DailyGoalQueryRepository;
import com.ject.studytrip.trip.domain.repository.DailyGoalRepository;
import com.ject.studytrip.trip.fixture.DailyGoalFixture;
import com.ject.studytrip.trip.fixture.TripFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DailyGoalCommandService 단위 테스트")
class DailyGoalCommandServiceTest extends BaseUnitTest {
    @InjectMocks private DailyGoalCommandService dailyGoalCommandService;
    @Mock private DailyGoalRepository dailyGoalRepository;
    @Mock private DailyGoalQueryRepository dailyGoalQueryRepository;

    private Trip trip;
    private DailyGoal dailyGoal;

    @BeforeEach
    void setUp() {
        Member member = MemberFixture.createMemberFromKakaoWithId(1L);
        trip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
        dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, trip);
    }

    @Nested
    @DisplayName("createDailyGoal 메서드는")
    class CreateDailyGoal {

        @Test
        @DisplayName("여행에 속한 데일리 목표를 생성하고 저장된 값을 반환한다")
        void shouldCreateAndSaveDailyGoal() {
            // given
            String title = "TEST TITLE";
            given(dailyGoalRepository.save(any())).willReturn(dailyGoal);

            // when
            DailyGoal result = dailyGoalCommandService.createDailyGoal(trip, title);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTrip().getId()).isEqualTo(trip.getId());
        }
    }

    @Nested
    @DisplayName("deleteDailyGoal 메서드는")
    class DeleteDailyGoal {

        @Test
        @DisplayName("deletedAt을 현재 시간으로 설정한다")
        void shouldSoftDeleteDailyGoal() {
            // when
            dailyGoalCommandService.deleteDailyGoal(dailyGoal);

            // then
            assertThat(dailyGoal.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("hardDeleteDailyGoals 메서드는")
    class HardDeleteDailyGoals {

        @Test
        @DisplayName("삭제된 데일리 목표가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDeletedDailyGoalsDoNotExist() {
            // given
            given(dailyGoalQueryRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L);

            // when
            long result = dailyGoalCommandService.hardDeleteDailyGoals();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 데일리 목표가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDeletedDailyGoalsExist() {
            // given
            given(dailyGoalQueryRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L);

            // when
            long result = dailyGoalCommandService.hardDeleteDailyGoals();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteDailyGoalsOwnedByDeletedTrip 메서드는")
    class HardDeleteDailyGoalsOwnedByDeletedTrip {

        @Test
        @DisplayName("삭제된 여행이 소유한 데일리 목표가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDailyGoalsOwnedByDeletedTripDoNotExist() {
            // given
            given(dailyGoalQueryRepository.deleteAllByDeletedTripOwner()).willReturn(0L);

            // when
            long result = dailyGoalCommandService.hardDeleteDailyGoalsOwnedByDeletedTrip();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 여행이 소유한 데일리 목표가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDailyGoalsOwnedByDeletedTripExist() {
            // given
            given(dailyGoalQueryRepository.deleteAllByDeletedTripOwner()).willReturn(5L);

            // when
            long result = dailyGoalCommandService.hardDeleteDailyGoalsOwnedByDeletedTrip();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }
}
