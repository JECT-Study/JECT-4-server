package com.ject.studytrip.pomodoro.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.pomodoro.domain.error.PomodoroErrorCode
import com.ject.studytrip.pomodoro.domain.model.Pomodoro
import com.ject.studytrip.pomodoro.domain.repository.PomodoroQueryRepository
import com.ject.studytrip.pomodoro.domain.repository.PomodoroRepository
import com.ject.studytrip.pomodoro.fixture.PomodoroFixture
import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.Trip
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
import java.util.Optional

@DisplayName("PomodoroQueryService 단위 테스트")
class PomodoroQueryServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var pomodoroQueryService: PomodoroQueryService

    @Mock
    private lateinit var pomodoroRepository: PomodoroRepository

    @Mock
    private lateinit var pomodoroQueryRepository: PomodoroQueryRepository

    private lateinit var trip: Trip
    private lateinit var dailyGoal: DailyGoal
    private lateinit var pomodoro: Pomodoro

    @BeforeEach
    fun setUp() {
        val member = MemberFixture.createMemberFromKakaoWithId(1L)
        trip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
        dailyGoal = DailyGoalFixture(trip).createWithId(1L)
        pomodoro = PomodoroFixture(dailyGoal).createWithId(1L)
    }

    @Nested
    @DisplayName("getValidPomodoroByDailyGoalId 메서드는")
    inner class GetValidPomodoroByDailyGoalId {
        @Test
        @DisplayName("뽀모도로가 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenPomodoroDoesNotExist() {
            // given
            val dailyGoalId = -1L
            given(pomodoroRepository.findByDailyGoalId(dailyGoalId)).willReturn(Optional.empty())

            // when
            val exception =
                assertThrows<CustomException> {
                    pomodoroQueryService.getValidPomodoroByDailyGoalId(dailyGoalId)
                }

            // then
            assertThat(exception.message).isEqualTo(PomodoroErrorCode.POMODORO_NOT_FOUND.message)
        }

        @Test
        @DisplayName("뽀모도로가 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenPomodoroAlreadyDeleted() {
            // given
            val dailyGoalId = dailyGoal.id
            pomodoro.updateDeletedAt()
            given(pomodoroRepository.findByDailyGoalId(dailyGoalId)).willReturn(Optional.of(pomodoro))

            // when
            val exception =
                assertThrows<CustomException> {
                    pomodoroQueryService.getValidPomodoroByDailyGoalId(dailyGoalId)
                }

            // then
            assertThat(exception.message).isEqualTo(PomodoroErrorCode.POMODORO_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("데일리 목표 ID로 뽀모도로를 조회하고 반환한다.")
        fun shouldReturnPomodoroByDailyGoalId() {
            // given
            val dailyGoalId = dailyGoal.id
            given(pomodoroRepository.findByDailyGoalId(dailyGoalId)).willReturn(Optional.of(pomodoro))

            // when
            val result = pomodoroQueryService.getValidPomodoroByDailyGoalId(dailyGoalId)

            // then
            assertThat(result).isEqualTo(pomodoro)
            assertThat(result.dailyGoal.id).isEqualTo(dailyGoalId)
        }
    }

    @Nested
    @DisplayName("getTotalFocusHoursByTripId 메서드는")
    inner class GetTotalFocusHoursByTripId {
        @Test
        @DisplayName("유효하지 않은 여행 ID가 들어오면 0을 반환한다.")
        fun shouldReturnZeroWhenTripIdIsInvalid() {
            // given
            val tripId = -1L
            given(pomodoroQueryRepository.sumFocusHoursByTripId(tripId)).willReturn(0L)

            // when
            val result = pomodoroQueryService.getTotalFocusHoursByTripId(tripId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("유효한 여행 ID가 들어오면 총 집중 시간(시간 단위)을 반환한다.")
        fun shouldReturnTotalFocusHoursWhenTripIdIsValid() {
            // given
            val tripId = trip.id
            given(pomodoroQueryRepository.sumFocusHoursByTripId(tripId)).willReturn(100L)

            // when
            val result = pomodoroQueryService.getTotalFocusHoursByTripId(tripId)

            // then
            assertThat(result).isEqualTo(100L)
        }
    }
}
