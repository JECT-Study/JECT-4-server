package com.ject.studytrip.pomodoro.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.pomodoro.domain.error.PomodoroErrorCode;
import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import com.ject.studytrip.pomodoro.domain.repository.PomodoroCommandRepository;
import com.ject.studytrip.pomodoro.domain.repository.PomodoroRepository;
import com.ject.studytrip.pomodoro.fixture.PomodoroFixture;
import com.ject.studytrip.pomodoro.presentation.dto.request.CreatePomodoroRequest;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.DailyGoalFixture;
import com.ject.studytrip.trip.fixture.TripFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("PomodoroCommandService 단위 테스트")
public class PomodoroCommandServiceTest extends BaseUnitTest {
    @InjectMocks private PomodoroCommandService pomodoroCommandService;
    @Mock private PomodoroRepository pomodoroRepository;
    @Mock private PomodoroCommandRepository pomodoroCommandRepository;

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
    @DisplayName("createPomodoro 메서드는")
    class CreatePomodoro {

        @Test
        @DisplayName("뽀모도로를 생성해 저장하고 반환한다")
        void shouldCreateAndReturnPomodoro() {
            // given
            CreatePomodoroRequest request = new CreatePomodoroRequest(30, 1);
            given(pomodoroRepository.save(any())).willReturn(pomodoro);

            // when
            Pomodoro result = pomodoroCommandService.createPomodoro(dailyGoal, request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getFocusDurationInSeconds()).isEqualTo(30 * 60);
            assertThat(result.getFocusSessionCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("updateTotalFocusTime 메서드는")
    class UpdateTotalFocusTime {

        @Test
        @DisplayName("뽀모도로 총 집중시간(분)이 음수일 경우 예외가 발생한다")
        void shouldThrowExceptionWhenTotalFocusTimeIsNegative() {
            // given
            int totalFocusTimeInSeconds = -30;

            // when & then
            assertThatThrownBy(
                            () ->
                                    pomodoroCommandService.updateTotalFocusTime(
                                            pomodoro, totalFocusTimeInSeconds))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(PomodoroErrorCode.POMODORO_NEGATIVE_FOCUS_TIME.getMessage());
        }

        @Test
        @DisplayName("유효한 뽀모도로와 총 학습시간으로 뽀모도로의 총 학습시간을 업데이트한다")
        void shouldUpdateTotalFocusTime() {
            // given
            int totalFocusTimeInSeconds = 120;

            // when
            pomodoroCommandService.updateTotalFocusTime(pomodoro, totalFocusTimeInSeconds);

            // then
            assertThat(pomodoro.getTotalFocusTimeInSeconds()).isEqualTo(totalFocusTimeInSeconds);
        }
    }

    @Nested
    @DisplayName("deletePomodoro 메서드는")
    class DeletePomodoro {

        @Test
        @DisplayName("deletedAt 필드를 설정해 삭제 처리한다")
        void shouldSoftDeletePomodoro() {
            // given
            assertThat(pomodoro.getDeletedAt()).isNull();

            // when
            pomodoroCommandService.deletePomodoro(pomodoro);

            // then
            assertThat(pomodoro.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("hardDeletePomodoros 메서드는")
    class HardDeletePomodoros {

        @Test
        @DisplayName("삭제된 뽀모도로가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDeletedPomodorosDoNotExist() {
            // given
            given(pomodoroCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L);

            // when
            long result = pomodoroCommandService.hardDeletePomodoros();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 뽀모도로가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDeletedPomodorosExist() {
            // given
            given(pomodoroCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L);

            // when
            long result = pomodoroCommandService.hardDeletePomodoros();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeletePomodorosOwnedByDeletedDailyGoal 메서드는")
    class HardDeletePomodorosOwnedByDeletedDailyGoal {

        @Test
        @DisplayName("삭제된 데일리 목표가 소유한 뽀모도로가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenPomodorosOwnedByDeletedDailyGoal() {
            // given
            given(pomodoroCommandRepository.deleteAllByDeletedDailyGoalOwner()).willReturn(0L);

            // when
            long result = pomodoroCommandService.hardDeletePomodorosOwnedByDeletedDailyGoal();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 데일리 목표가 소유한 뽀모도로가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenPomodorosOwnedByDeletedDailyGoal() {
            // given
            given(pomodoroCommandRepository.deleteAllByDeletedDailyGoalOwner()).willReturn(5L);

            // when
            long result = pomodoroCommandService.hardDeletePomodorosOwnedByDeletedDailyGoal();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeletePomodorosByMember 메서드는")
    class HardDeletePomodorosByMember {

        @Test
        @DisplayName("특정 멤버가 소유한 뽀모도로가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenPomodorosOwnedByMemberDoNotExist() {
            // given
            Long memberId = 1L;
            given(pomodoroCommandRepository.deleteAllByMemberId(memberId)).willReturn(0L);

            // when
            long result = pomodoroCommandService.hardDeletePomodorosByMember(memberId);

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("특정 멤버가 소유한 뽀모도로가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenPomodorosOwnedByMemberExist() {
            // given
            Long memberId = 1L;
            given(pomodoroCommandRepository.deleteAllByMemberId(memberId)).willReturn(5L);

            // when
            long result = pomodoroCommandService.hardDeletePomodorosByMember(memberId);

            // then
            assertThat(result).isEqualTo(5L);
        }
    }
}
