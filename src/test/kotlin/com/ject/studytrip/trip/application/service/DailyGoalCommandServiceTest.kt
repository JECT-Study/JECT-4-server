package com.ject.studytrip.trip.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.domain.repository.DailyGoalCommandRepository
import com.ject.studytrip.trip.domain.repository.DailyGoalRepository
import com.ject.studytrip.trip.fixture.DailyGoalFixture
import com.ject.studytrip.trip.fixture.TripFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any

@DisplayName("DailyGoalCommandService 단위 테스트")
class DailyGoalCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var dailyGoalCommandService: DailyGoalCommandService

    @Mock
    private lateinit var dailyGoalRepository: DailyGoalRepository

    @Mock
    private lateinit var dailyGoalCommandRepository: DailyGoalCommandRepository

    private lateinit var member: Member
    private lateinit var trip: Trip
    private lateinit var dailyGoal: DailyGoal

    @BeforeEach
    fun setUp() {
        member = MemberFixture().createFromKakaoWithId(1L)
        trip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
        dailyGoal = DailyGoalFixture(trip).createWithId(1L)
    }

    @Nested
    @DisplayName("createDailyGoal 메서드는")
    inner class CreateDailyGoal {
        @Test
        @DisplayName("데일리 목표를 생성하고 반환한다.")
        fun shouldCreateAndReturnDailyGoal() {
            // given
            val title = dailyGoal.title
            given(dailyGoalRepository.save(any())).willReturn(dailyGoal)

            // when
            val result = dailyGoalCommandService.createDailyGoal(trip, title)

            // then
            assertThat(result).isEqualTo(dailyGoal)
            assertThat(dailyGoal.trip).isEqualTo(trip)
        }
    }

    @Nested
    @DisplayName("deleteDailyGoal 메서드는")
    inner class DeleteDailyGoal {
        @Test
        @DisplayName("데일리 목표가 삭제될 때 deletedAt 필드를 현재 시간으로 업데이트한다. (소프트 삭제)")
        fun shouldUpdateDeletedAtWhenDailyGoalIsDeleted() {
            // when
            dailyGoalCommandService.deleteDailyGoal(dailyGoal)

            // then
            assertThat(dailyGoal.deletedAt).isNotNull
        }
    }

    @Nested
    @DisplayName("hardDeleteDailyGoals 메서드는")
    inner class HardDeleteDailyGoals {
        @Test
        @DisplayName("삭제된 데일리 목표가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDeletedDailyGoalsDoNotExist() {
            // given
            given(dailyGoalCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L)

            // when
            val result = dailyGoalCommandService.hardDeleteDailyGoals()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 데일리 목표가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDeletedDailyGoalsExist() {
            // given
            given(dailyGoalCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L)

            // when
            val result = dailyGoalCommandService.hardDeleteDailyGoals()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteDailyGoalsOwnedByDeletedTrip 메서드는")
    inner class HardDeleteDailyGoalsOwnedByDeletedTrip {
        @Test
        @DisplayName("삭제된 여행이 소유한 데일리 목표가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDailyGoalsOwnedByDeletedTripDoNotExist() {
            // given
            given(dailyGoalCommandRepository.deleteAllByDeletedTripOwner()).willReturn(0L)

            // when
            val result = dailyGoalCommandService.hardDeleteDailyGoalsOwnedByDeletedTrip()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 여행이 소유한 데일리 목표가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDailyGoalsOwnedByDeletedTripExist() {
            // given
            given(dailyGoalCommandRepository.deleteAllByDeletedTripOwner()).willReturn(5L)

            // when
            val result = dailyGoalCommandService.hardDeleteDailyGoalsOwnedByDeletedTrip()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteDailyGoalsOwnedByMember 메서드는")
    inner class HardDeleteDailyGoalsOwnedByMember {
        @Test
        @DisplayName("특정 멤버가 소유한 데일리 목표가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDailyGoalsOwnedByMemberDoNotExist() {
            // given
            val memberId = member.id.requireId()
            given(dailyGoalCommandRepository.deleteAllByMemberId(memberId)).willReturn(0L)

            // when
            val result = dailyGoalCommandService.hardDeleteDailyGoalsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 멤버가 소유한 데일리 목표가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDailyGoalsOwnedByMemberExist() {
            // given
            val memberId = member.id.requireId()
            given(dailyGoalCommandRepository.deleteAllByMemberId(memberId)).willReturn(5L)

            // when
            val result = dailyGoalCommandService.hardDeleteDailyGoalsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(5L)
        }
    }
}
