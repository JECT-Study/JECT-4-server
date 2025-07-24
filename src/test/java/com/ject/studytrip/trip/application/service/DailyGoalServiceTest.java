package com.ject.studytrip.trip.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DailyGoalService 단위 테스트")
public class DailyGoalServiceTest extends BaseUnitTest {

    @InjectMocks private DailyGoalService dailyGoalService;
    @Mock private DailyGoalRepository dailyGoalRepository;

    private Member member;
    private Trip trip;
    private DailyGoal dailyGoal;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMemberFromKakaoWithId(1L);
        trip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
        dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, trip);
    }

    @Nested
    @DisplayName("데일리 목표를 생성한다")
    class CreateDailyGoal {

        @Test
        @DisplayName("여행에 속한 데일리 목표를 생성하고 저장된 값을 반환한다")
        void shouldCreateAndSaveDailyGoal() {
            // given
            given(dailyGoalRepository.save(any())).willReturn(dailyGoal);

            // when
            DailyGoal result = dailyGoalService.createDailyGoal(trip);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTrip().getId()).isEqualTo(trip.getId());
        }
    }

    @Nested
    @DisplayName("데일리 목표를 삭제한다")
    class DeleteDailyGoal {

        @Test
        @DisplayName("deletedAt을 현재 시간으로 설정한다")
        void shouldSoftDeleteDailyGoal() {
            // when
            dailyGoalService.deleteDailyGoal(dailyGoal);

            // then
            assertThat(dailyGoal.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("데일리 목표를 조회한다")
    class GetDailyGoal {

        @Test
        @DisplayName("ID로 조회된 데일리 목표가 trip에 속하고 삭제되지 않았다면 반환한다")
        void shouldReturnValidDailyGoal() {
            // given
            given(dailyGoalRepository.findById(dailyGoal.getId()))
                    .willReturn(Optional.of(dailyGoal));

            // when
            DailyGoal result = dailyGoalService.getValidDailyGoal(trip.getId(), dailyGoal.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(dailyGoal.getId());
        }

        @Test
        @DisplayName("데일리 목표가 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenDailyGoalNotFound() {
            // given
            given(dailyGoalRepository.findById(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> dailyGoalService.getValidDailyGoal(trip.getId(), 999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("다른 여행에 속한 데일리 목표일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenNotBelongToTrip() {
            // given
            Trip otherTrip = TripFixture.createTripWithId(999L, member, TripCategory.COURSE);
            given(dailyGoalRepository.findById(dailyGoal.getId()))
                    .willReturn(Optional.of(dailyGoal));

            // when & then
            assertThatThrownBy(
                            () ->
                                    dailyGoalService.getValidDailyGoal(
                                            otherTrip.getId(), dailyGoal.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DailyGoalErrorCode.DAILY_GOAL_NOT_BELONG_TO_TRIP.getMessage());
        }

        @Test
        @DisplayName("삭제된 데일리 목표일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenDeletedDailyGoal() {
            // given
            DailyGoal deleted = DailyGoalFixture.createDeletedDailyGoal(trip);
            given(dailyGoalRepository.findById(deleted.getId())).willReturn(Optional.of(deleted));

            // when & then
            assertThatThrownBy(
                            () -> dailyGoalService.getValidDailyGoal(trip.getId(), deleted.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED.getMessage());
        }
    }
}
