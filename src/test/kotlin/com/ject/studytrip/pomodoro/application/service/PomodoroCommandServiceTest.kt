package com.ject.studytrip.pomodoro.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.pomodoro.domain.error.PomodoroErrorCode
import com.ject.studytrip.pomodoro.domain.model.Pomodoro
import com.ject.studytrip.pomodoro.domain.repository.PomodoroCommandRepository
import com.ject.studytrip.pomodoro.domain.repository.PomodoroRepository
import com.ject.studytrip.pomodoro.fixture.CreatePomodoroRequestFixture
import com.ject.studytrip.pomodoro.fixture.PomodoroFixture
import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.fixture.DailyGoalFixture
import com.ject.studytrip.trip.fixture.TripFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any

@DisplayName("PomodoroCommandService 단위 테스트")
class PomodoroCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var pomodoroCommandService: PomodoroCommandService

    @Mock
    private lateinit var pomodoroRepository: PomodoroRepository

    @Mock
    private lateinit var pomodoroCommandRepository: PomodoroCommandRepository

    private lateinit var member: Member
    private lateinit var dailyGoal: DailyGoal
    private lateinit var pomodoro: Pomodoro

    @BeforeEach
    fun setUp() {
        member = MemberFixture.createMemberFromKakaoWithId(1L)
        val trip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE)
        dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, trip)
        pomodoro = PomodoroFixture(dailyGoal).createWithId(1L)
    }

    @Nested
    @DisplayName("createPomodoro 메서드는")
    inner class CreatePomodoro {
        @Test
        @DisplayName("유효한 요청이 들어오면 뽀모도로를 생성하고 반환한다.")
        fun shouldCreateAndReturnPomodoroWhenRequestIsValid() {
            // given
            val request =
                CreatePomodoroRequestFixture()
                    .apply {
                        focusDurationInMinute = 30
                        focusSessionCount = 1
                    }.build()
            given(pomodoroRepository.save(any())).willReturn(pomodoro)

            // when
            val result = pomodoroCommandService.createPomodoro(dailyGoal, request)

            // then
            assertThat(result).isEqualTo(pomodoro)
            assertThat(result.focusDurationInSeconds).isEqualTo(30 * 60)
            assertThat(result.focusSessionCount).isEqualTo(1)
        }
    }

    @Nested
    @DisplayName("updateTotalFocusTime 메서드는")
    inner class UpdateTotalFocusTime {
        @Test
        @DisplayName("뽀모도로가 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenPomodoroAlreadyDeleted() {
            // given
            val totalFocusTimeInSeconds = 120
            pomodoro.updateDeletedAt()

            // when
            val exception =
                assertThrows<CustomException> {
                    pomodoroCommandService.updateTotalFocusTime(pomodoro, totalFocusTimeInSeconds)
                }

            // then
            assertThat(exception.message).isEqualTo(PomodoroErrorCode.POMODORO_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("뽀모도로 총 집중시간(분)이 음수라면 예외가 발생한다.")
        fun shouldThrowExceptionWhenTotalFocusTimeIsNegative() {
            // given
            val totalFocusTimeInSeconds = -30

            // when
            val exception =
                assertThrows<CustomException> {
                    pomodoroCommandService.updateTotalFocusTime(pomodoro, totalFocusTimeInSeconds)
                }

            // then
            assertThat(exception.message).isEqualTo(PomodoroErrorCode.POMODORO_NEGATIVE_FOCUS_TIME.message)
        }

        @Test
        @DisplayName("뽀모도로와 총 집중시간(분)이 유효하면 총 집중시간을 업데이트한다.")
        fun shouldUpdateTotalFocusTimeWhenPomodoroAndTotalFocusTimeIsValid() {
            // given
            val totalFocusTimeInSeconds = 120

            // when
            pomodoroCommandService.updateTotalFocusTime(pomodoro, totalFocusTimeInSeconds)

            // then
            assertThat(pomodoro.totalFocusTimeInSeconds).isEqualTo(totalFocusTimeInSeconds)
        }
    }

    @Nested
    @DisplayName("deletePomodoro 메서드는")
    inner class DeletePomodoro {
        @Test
        @DisplayName("뽀모도로가 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenPomodoroAlreadyDeleted() {
            // given
            pomodoro.updateDeletedAt()

            // when
            val exception =
                assertThrows<CustomException> {
                    pomodoroCommandService.deletePomodoro(pomodoro)
                }

            // then
            assertThat(exception.message).isEqualTo(PomodoroErrorCode.POMODORO_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("뽀모도로가 삭제될 때 deletedAt 필드를 현재 시간으로 업데이트한다. (소프트 삭제)")
        fun shouldUpdateDeletedAtWhenPomodoroIsDeleted() {
            // when
            pomodoroCommandService.deletePomodoro(pomodoro)

            // then
            assertThat(pomodoro.deletedAt).isNotNull
        }
    }

    @Nested
    @DisplayName("hardDeletePomodoros 메서드는")
    inner class HardDeletePomodoros {
        @Test
        @DisplayName("삭제된 뽀모도로가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDeletedPomodorosDoNotExist() {
            // given
            given(pomodoroCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L)

            // when
            val result = pomodoroCommandService.hardDeletePomodoros()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 뽀모도로가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDeletedPomodorosExist() {
            // given
            given(pomodoroCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L)

            // when
            val result = pomodoroCommandService.hardDeletePomodoros()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeletePomodorosOwnedByDeletedDailyGoal 메서드는")
    inner class HardDeletePomodorosOwnedByDeletedDailyGoal {
        @Test
        @DisplayName("삭제된 데일리 목표가 소유한 뽀모도로가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenPomodorosOwnedByDeletedDailyGoalDoNotExist() {
            // given
            given(pomodoroCommandRepository.deleteAllByDeletedDailyGoalOwner()).willReturn(0L)

            // when
            val result = pomodoroCommandService.hardDeletePomodorosOwnedByDeletedDailyGoal()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 데일리 목표가 소유한 뽀모도로가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenPomodorosOwnedByDeletedDailyGoalExist() {
            // given
            given(pomodoroCommandRepository.deleteAllByDeletedDailyGoalOwner()).willReturn(5L)

            // when
            val result = pomodoroCommandService.hardDeletePomodorosOwnedByDeletedDailyGoal()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeletePomodorosOwnedByMember 메서드는")
    inner class HardDeletePomodorosOwnedByMember {
        @Test
        @DisplayName("특정 멤버가 소유한 뽀모도로가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenPomodorosOwnedByMemberDoNotExist() {
            // given
            val memberId = member.id
            given(pomodoroCommandRepository.deleteAllByMemberId(memberId)).willReturn(0L)

            // when
            val result = pomodoroCommandService.hardDeletePomodorosOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 멤버가 소유한 뽀모도로가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenPomodorosOwnedByMemberExist() {
            // given
            val memberId = member.id
            given(pomodoroCommandRepository.deleteAllByMemberId(memberId)).willReturn(5L)

            // when
            val result = pomodoroCommandService.hardDeletePomodorosOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(5L)
        }
    }
}
