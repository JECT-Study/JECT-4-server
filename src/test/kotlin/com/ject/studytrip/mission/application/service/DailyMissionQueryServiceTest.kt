package com.ject.studytrip.mission.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.mission.domain.error.DailyMissionErrorCode
import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.mission.domain.repository.DailyMissionQueryRepository
import com.ject.studytrip.mission.domain.repository.DailyMissionRepository
import com.ject.studytrip.mission.fixture.DailyMissionFixture
import com.ject.studytrip.mission.fixture.MissionFixture
import com.ject.studytrip.stamp.fixture.StampFixture
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

@DisplayName("DailyMissionQueryService 단위 테스트")
class DailyMissionQueryServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var dailyMissionQueryService: DailyMissionQueryService

    @Mock
    private lateinit var dailyMissionRepository: DailyMissionRepository

    @Mock
    private lateinit var dailyMissionQueryRepository: DailyMissionQueryRepository

    private lateinit var trip: Trip
    private lateinit var dailyGoal: DailyGoal
    private lateinit var dailyMission: DailyMission

    @BeforeEach
    fun setUp() {
        val member = MemberFixture().createFromKakaoWithId(1L)
        trip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
        val stamp = StampFixture(trip, 1).createWithId(1L)
        val mission = MissionFixture(stamp).createWithId(1L)
        dailyGoal = DailyGoalFixture(trip).createWithId(1L)
        dailyMission = DailyMissionFixture(mission, dailyGoal).createWithId(1L)
    }

    @Nested
    @DisplayName("getValidDailyMissionsByIds 메서드는")
    inner class GetValidDailyMissionsByIds {
        @Test
        @DisplayName("요청한 데일리 미션 ID 개수와 조회된 데일리 미션 개수가 일치하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenSomeDailyMissionsDoNotExist() {
            // given
            val dailyMissionIds = listOf(dailyMission.id, 1000L)
            given(dailyMissionRepository.findAllByIdIn(dailyMissionIds)).willReturn(listOf(dailyMission))

            // when
            val exception =
                assertThrows<CustomException> { dailyMissionQueryService.getValidDailyMissionsByIds(dailyGoal.id, dailyMissionIds) }

            // then
            assertThat(exception.message).isEqualTo(DailyMissionErrorCode.DAILY_MISSION_NOT_FOUND.message)
        }

        @Test
        @DisplayName("특정 데일리 목표에 속하지 않은 데일리 미션이 하나라도 존재하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenDailyMissionsNotBelongToDailyGoal() {
            // given
            val newDailyGoal = DailyGoalFixture(trip).createWithId(2L)
            val dailyMissionIds = listOf(dailyMission.id)
            given(dailyMissionRepository.findAllByIdIn(dailyMissionIds)).willReturn(listOf(dailyMission))

            // when
            val exception =
                assertThrows<CustomException> { dailyMissionQueryService.getValidDailyMissionsByIds(newDailyGoal.id, dailyMissionIds) }

            // then
            assertThat(exception.message).isEqualTo(DailyMissionErrorCode.DAILY_MISSION_NOT_BELONGS_TO_DAILY_GOAL.message)
        }

        @Test
        @DisplayName("데일리 미션이 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenDailyMissionAlreadyDeleted() {
            // given
            val dailyMissionIds = listOf(dailyMission.id)
            dailyMission.updateDeletedAt()
            given(dailyMissionRepository.findAllByIdIn(dailyMissionIds)).willReturn(listOf(dailyMission))

            // when
            val exception =
                assertThrows<CustomException> { dailyMissionQueryService.getValidDailyMissionsByIds(dailyGoal.id, dailyMissionIds) }

            // then
            assertThat(exception.message).isEqualTo(DailyMissionErrorCode.DAILY_MISSION_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("데일리 미션 ID 목록과 일치하는 데일리 미션 목록을 조회하고 반환한다.")
        fun shouldReturnDailyMissionsByIds() {
            // given
            val dailyMissionIds = listOf(dailyMission.id)
            given(dailyMissionRepository.findAllByIdIn(dailyMissionIds)).willReturn(listOf(dailyMission))

            // when
            val result = dailyMissionQueryService.getValidDailyMissionsByIds(dailyGoal.id, dailyMissionIds)

            // then
            assertThat(result).hasSize(1)
            assertThat(result).contains(dailyMission)
        }
    }

    @Nested
    @DisplayName("getValidDailyMissionsWithMissionAndStampByIds 메서드는")
    inner class GetValidDailyMissionsWithMissionAndStampByIds {
        @Test
        @DisplayName("요청한 데일리 미션 ID 개수와 조회된 데일리 미션 개수가 일치하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenSomeDailyMissionsDoNotExist() {
            // given
            val dailyMissionIds = listOf(dailyMission.id, 1000L)
            given(dailyMissionQueryRepository.findAllWithMissionAndStampByIds(dailyMissionIds)).willReturn(listOf(dailyMission))

            // when
            val exception =
                assertThrows<CustomException> {
                    dailyMissionQueryService.getValidDailyMissionsWithMissionAndStampByIds(
                        dailyGoal.id,
                        dailyMissionIds,
                    )
                }

            // then
            assertThat(exception.message).isEqualTo(DailyMissionErrorCode.DAILY_MISSION_NOT_FOUND.message)
        }

        @Test
        @DisplayName("특정 데일리 목표에 속하지 않은 데일리 미션이 하나라도 존재하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenDailyMissionsNotBelongToDailyGoal() {
            // given
            val newDailyGoal = DailyGoalFixture(trip).createWithId(2L)
            val dailyMissionIds = listOf(dailyMission.id)
            given(dailyMissionQueryRepository.findAllWithMissionAndStampByIds(dailyMissionIds)).willReturn(listOf(dailyMission))

            // when
            val exception =
                assertThrows<CustomException> {
                    dailyMissionQueryService.getValidDailyMissionsWithMissionAndStampByIds(
                        newDailyGoal.id,
                        dailyMissionIds,
                    )
                }

            // then
            assertThat(exception.message).isEqualTo(DailyMissionErrorCode.DAILY_MISSION_NOT_BELONGS_TO_DAILY_GOAL.message)
        }

        @Test
        @DisplayName("데일리 미션이 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenDailyMissionAlreadyDeleted() {
            // given
            val dailyMissionIds = listOf(dailyMission.id)
            dailyMission.updateDeletedAt()
            given(dailyMissionQueryRepository.findAllWithMissionAndStampByIds(dailyMissionIds)).willReturn(listOf(dailyMission))

            // when
            val exception =
                assertThrows<CustomException> {
                    dailyMissionQueryService.getValidDailyMissionsWithMissionAndStampByIds(
                        dailyGoal.id,
                        dailyMissionIds,
                    )
                }

            // then
            assertThat(exception.message).isEqualTo(DailyMissionErrorCode.DAILY_MISSION_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("데일리 미션 ID 목록과 일치하는 데일리 미션 목록을 미션과 스탬프와 함께 조회하고 반환한다.")
        fun shouldReturnDailyMissionsWithMissionAndStampByIds() {
            // given
            val dailyMissionIds = listOf(dailyMission.id)
            given(dailyMissionQueryRepository.findAllWithMissionAndStampByIds(dailyMissionIds)).willReturn(listOf(dailyMission))

            // when
            val result = dailyMissionQueryService.getValidDailyMissionsWithMissionAndStampByIds(dailyGoal.id, dailyMissionIds)

            // then
            assertThat(result).hasSize(1)
            assertThat(result).contains(dailyMission)
        }
    }

    @Nested
    @DisplayName("getDailyMissionsByDailyGoalId 메서드는")
    inner class GetDailyMissionsByDailyGoalId {
        @Test
        @DisplayName("데일리 목표 ID로 데일리 미션 목록을 조회하고 반환한다.")
        fun shouldReturnDailyMissionsByDailyGoalId() {
            // given
            val dailyGoalId = dailyGoal.id
            given(dailyMissionQueryRepository.findAllByDailyGoalIdFetchJoinMission(dailyGoalId)).willReturn(listOf(dailyMission))

            // when
            val result = dailyMissionQueryService.getDailyMissionsByDailyGoalId(dailyGoalId)

            // then
            assertThat(result).hasSize(1)
            assertThat(result).contains(dailyMission)
        }
    }
}
