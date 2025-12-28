package com.ject.studytrip.mission.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.mission.domain.repository.DailyMissionCommandRepository
import com.ject.studytrip.mission.domain.repository.DailyMissionRepository
import com.ject.studytrip.mission.fixture.DailyMissionFixture
import com.ject.studytrip.mission.fixture.MissionFixture
import com.ject.studytrip.stamp.fixture.StampFixture
import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.fixture.DailyGoalFixture
import com.ject.studytrip.trip.fixture.TripFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock

@DisplayName("DailyMissionCommandService 단위 테스트")
class DailyMissionCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var dailyMissionCommandService: DailyMissionCommandService

    @Mock
    private lateinit var dailyMissionRepository: DailyMissionRepository

    @Mock
    private lateinit var dailyMissionCommandRepository: DailyMissionCommandRepository

    private lateinit var member: Member
    private lateinit var mission: Mission
    private lateinit var dailyGoal: DailyGoal
    private lateinit var dailyMission: DailyMission

    @BeforeEach
    fun setUp() {
        member = MemberFixture.createMemberFromKakaoWithId(1L)
        val trip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
        val stamp = StampFixture(trip, 1).createWithId(1L)
        mission = MissionFixture(stamp).createWithId(1L)
        dailyGoal = DailyGoalFixture(trip).createWithId(1L)
        dailyMission = DailyMissionFixture(mission, dailyGoal).createWithId(1L)
    }

    @Nested
    @DisplayName("createDailyMissions 메서드는")
    inner class CreateDailyMissions {
        @Test
        @DisplayName("데일리 미션 목록을 생성하고 반환한다.")
        fun shouldCreateAndReturnDailyMissions() {
            // given
            val missions = listOf(mission)
            given(dailyMissionRepository.saveAll(anyList())).willReturn(listOf(dailyMission))

            // when
            val result = dailyMissionCommandService.createDailyMissions(dailyGoal, missions)

            // then
            assertThat(result).hasSize(missions.size)
            assertThat(result).contains(dailyMission)
        }
    }

    @Nested
    @DisplayName("deleteDailyMission 메서드는")
    inner class DeleteDailyMission {
        @Test
        @DisplayName("데일리 미션이 삭제될 때 deletedAt 필드를 현재 시간으로 업데이트한다. (소프트 삭제)")
        fun shouldUpdateDeletedAtWhenDailyMissionIsDeleted() {
            // when
            dailyMissionCommandService.deleteDailyMission(dailyMission)

            // then
            assertThat(dailyMission.deletedAt).isNotNull
        }
    }

    @Nested
    @DisplayName("hardDeleteDailyMissions 메서드는")
    inner class HardDeleteDailyMissions {
        @Test
        @DisplayName("삭제된 데일리 미션이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDeletedDailyMissionsDoNotExist() {
            // given
            given(dailyMissionCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L)

            // when
            val result = dailyMissionCommandService.hardDeleteDailyMissions()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 데일리 미션이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDeletedDailyMissionsExist() {
            // given
            given(dailyMissionCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L)

            // when
            val result = dailyMissionCommandService.hardDeleteDailyMissions()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteDailyMissionsOwnedByDeletedMission 메서드는")
    inner class HardDeleteDailyMissionsOwnedByDeletedMission {
        @Test
        @DisplayName("삭제된 미션이 소유한 데일리 미션이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDailyMissionsOwnedByDeletedMissionDoNotExist() {
            // given
            given(dailyMissionCommandRepository.deleteAllByDeletedMissionOwner()).willReturn(0L)

            // when
            val result = dailyMissionCommandService.hardDeleteDailyMissionsOwnedByDeletedMission()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 미션이 소유한 데일리 미션이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDailyMissionsOwnedByDeletedMissionExist() {
            // given
            given(dailyMissionCommandRepository.deleteAllByDeletedMissionOwner()).willReturn(5L)

            // when
            val result = dailyMissionCommandService.hardDeleteDailyMissionsOwnedByDeletedMission()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteDailyMissionsOwnedByDeletedDailyGoal 메서드는")
    inner class HardDeleteDailyMissionsOwnedByDeletedDailyGoal {
        @Test
        @DisplayName("삭제된 데일리 목표가 소유한 데일리 미션이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDailyMissionsOwnedByDeletedDailyGoalDoNotExist() {
            // given
            given(dailyMissionCommandRepository.deleteAllByDeletedDailyGoalOwner()).willReturn(0L)

            // when
            val result = dailyMissionCommandService.hardDeleteDailyMissionsOwnedByDeletedDailyGoal()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 데일리 목표가 소유한 데일리 미션이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDailyMissionsOwnedByDeletedDailyGoalExist() {
            // given
            given(dailyMissionCommandRepository.deleteAllByDeletedDailyGoalOwner()).willReturn(5L)

            // when
            val result = dailyMissionCommandService.hardDeleteDailyMissionsOwnedByDeletedDailyGoal()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteDailyMissionsOwnedByMember 메서드는")
    inner class HardDeleteDailyMissionsOwnedByMember {
        @Test
        @DisplayName("특정 멤버가 소유한 데일리 미션이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDailyMissionsOwnedByMemberDoNotExist() {
            // given
            val memberId = member.id
            given(dailyMissionCommandRepository.deleteAllByMemberId(memberId)).willReturn(0L)

            // when
            val result = dailyMissionCommandService.hardDeleteDailyMissionsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 멤버가 소유한 데일리 미션이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDailyMissionsOwnedByMemberExist() {
            // given
            val memberId = member.id
            given(dailyMissionCommandRepository.deleteAllByMemberId(memberId)).willReturn(5L)

            // when
            val result = dailyMissionCommandService.hardDeleteDailyMissionsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(5L)
        }
    }
}
