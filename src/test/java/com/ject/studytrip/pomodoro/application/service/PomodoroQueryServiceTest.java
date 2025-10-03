package com.ject.studytrip.pomodoro.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.pomodoro.domain.error.PomodoroErrorCode;
import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import com.ject.studytrip.pomodoro.domain.repository.PomodoroRepository;
import com.ject.studytrip.pomodoro.fixture.PomodoroFixture;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.DailyGoalFixture;
import com.ject.studytrip.trip.fixture.TripFixture;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("PomodoroQueryService 단위 테스트")
class PomodoroQueryServiceTest extends BaseUnitTest {
    @InjectMocks private PomodoroQueryService pomodoroQueryService;
    @Mock private PomodoroRepository pomodoroRepository;

    private DailyGoal dailyGoal;
    private Pomodoro pomodoro;

    @BeforeEach
    void setUp() {
        Member member = MemberFixture.createMemberFromKakaoWithId(1L);
        Trip trip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
        dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, trip);
        pomodoro = PomodoroFixture.createPomodoroWithId(1L, dailyGoal);
    }

    @Nested
    @DisplayName("getValidPomodoroByDailyGoal 메서드는")
    class GetValidPomodoroByDailyGoal {

        @Test
        @DisplayName("데일리 목표 ID로 뽀모도로를 조회해 반환한다")
        void shouldReturnPomodoroByDailyGoalId() {
            // given
            Long dailyGoalId = dailyGoal.getId();
            given(pomodoroRepository.findByDailyGoalId(dailyGoalId))
                    .willReturn(Optional.of(pomodoro));

            // when
            Pomodoro result = pomodoroQueryService.getValidPomodoroByDailyGoal(dailyGoalId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getDailyGoal().getId()).isEqualTo(dailyGoalId);
        }

        @Test
        @DisplayName("뽀모도로가 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionIfPomodoroNotFound() {
            // given
            Long invalidId = -1L;
            given(pomodoroRepository.findByDailyGoalId(invalidId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> pomodoroQueryService.getValidPomodoroByDailyGoal(invalidId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(PomodoroErrorCode.POMODORO_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("삭제된 뽀모도로일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenDeletedPomodoro() {
            // given
            Long dailyGoalId = dailyGoal.getId();
            pomodoro.updateDeletedAt();
            given(pomodoroRepository.findByDailyGoalId(dailyGoalId))
                    .willReturn(Optional.of(pomodoro));

            // when & then
            assertThatThrownBy(() -> pomodoroQueryService.getValidPomodoroByDailyGoal(dailyGoalId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(PomodoroErrorCode.POMODORO_ALREADY_DELETED.getMessage());
        }
    }
}
