package com.ject.studytrip.trip.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.trip.domain.error.DailyGoalErrorCode
import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.domain.repository.DailyGoalRepository
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

@DisplayName("DailyGoalQueryService 단위 테스트")
class DailyGoalQueryServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var dailyGoalQueryService: DailyGoalQueryService

    @Mock
    private lateinit var dailyGoalRepository: DailyGoalRepository

    private lateinit var member: Member
    private lateinit var trip: Trip
    private lateinit var dailyGoal: DailyGoal

    @BeforeEach
    fun setUp() {
        member = MemberFixture.createMemberFromKakaoWithId(1L)
        trip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
        dailyGoal = DailyGoalFixture(trip).createWithId(1L)
    }

    @Nested
    @DisplayName("getValidDailyGoal 메서드는")
    inner class GetValidDailyGoal {
        @Test
        @DisplayName("데일리 목표가 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenDailyGoalDoesNotExist() {
            // given
            val dailyGoalId = -1L
            given(dailyGoalRepository.findById(dailyGoalId)).willReturn(Optional.empty())

            // when
            val exception = assertThrows<CustomException> { dailyGoalQueryService.getValidDailyGoal(trip.id, dailyGoalId) }

            // then
            assertThat(exception.message).isEqualTo(DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND.message)
        }

        @Test
        @DisplayName("특정 데일리 목표가 다른 여행에 속한다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenDailyGoalNotBelongToTrip() {
            // given
            val dailyGoalId = dailyGoal.id
            val newTrip = TripFixture(member, TripCategory.COURSE).createWithId(2L)
            given(dailyGoalRepository.findById(dailyGoalId)).willReturn(Optional.of(dailyGoal))

            // when
            val exception = assertThrows<CustomException> { dailyGoalQueryService.getValidDailyGoal(newTrip.id, dailyGoalId) }

            // then
            assertThat(exception.message).isEqualTo(DailyGoalErrorCode.DAILY_GOAL_NOT_BELONGS_TO_TRIP.message)
        }

        @Test
        @DisplayName("데일리 목표가 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenDailyGoalAlreadyDeleted() {
            // given
            val dailyGoalId = dailyGoal.id
            dailyGoal.updateDeletedAt()
            given(dailyGoalRepository.findById(dailyGoalId)).willReturn(Optional.of(dailyGoal))

            // when
            val exception = assertThrows<CustomException> { dailyGoalQueryService.getValidDailyGoal(trip.id, dailyGoalId) }

            // then
            assertThat(exception.message).isEqualTo(DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("특정 여행에 속한 데일리 목표가 존재하면 데일리 목표를 조회하고 반환한다.")
        fun shouldReturnDailyGoalWhenDailyGoalBelongsToTrip() {
            // given
            val dailyGoalId = dailyGoal.id
            given(dailyGoalRepository.findById(dailyGoalId)).willReturn(Optional.of(dailyGoal))

            // when
            val result = dailyGoalQueryService.getValidDailyGoal(trip.id, dailyGoalId)

            // then
            assertThat(result).isEqualTo(dailyGoal)
        }
    }
}
