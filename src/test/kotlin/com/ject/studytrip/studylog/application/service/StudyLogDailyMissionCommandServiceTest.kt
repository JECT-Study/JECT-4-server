package com.ject.studytrip.studylog.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.mission.fixture.DailyMissionFixture
import com.ject.studytrip.mission.fixture.MissionFixture
import com.ject.studytrip.stamp.fixture.StampFixture
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionCommandRepository
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionRepository
import com.ject.studytrip.studylog.fixture.StudyLogFixture
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

@DisplayName("StudyLogDailyMissionCommandService 단위 테스트")
class StudyLogDailyMissionCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var studyLogDailyMissionCommandService: StudyLogDailyMissionCommandService

    @Mock
    private lateinit var studyLogDailyMissionRepository: StudyLogDailyMissionRepository

    @Mock
    private lateinit var studyLogDailyMissionCommandRepository: StudyLogDailyMissionCommandRepository

    private lateinit var member: Member
    private lateinit var mission1: Mission
    private lateinit var mission2: Mission
    private lateinit var dailyGoal: DailyGoal
    private lateinit var dailyMission1: DailyMission
    private lateinit var dailyMission2: DailyMission
    private lateinit var studyLog: StudyLog

    @BeforeEach
    fun setUp() {
        member = MemberFixture().createFromKakaoWithId(1L)
        val trip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
        val stamp = StampFixture(trip, 1).createWithId(1L)
        mission1 = MissionFixture(stamp).createWithId(1L)
        mission2 = MissionFixture(stamp).createWithId(2L)
        dailyGoal = DailyGoalFixture(trip).createWithId(1L)
        dailyMission1 = DailyMissionFixture(mission1, dailyGoal).createWithId(1L)
        dailyMission2 = DailyMissionFixture(mission2, dailyGoal).createWithId(2L)
        studyLog = StudyLogFixture(member, dailyGoal).createWithId(1L)
    }

    @Nested
    @DisplayName("createStudyLogDailyMissions 메서드는")
    inner class CreateStudyLogDailyMissions {
        @Test
        @DisplayName("학습 로그와 데일리 미션 목록을 이용해 학습 로그 데일리 미션 목록을 생성하고 반환한다.")
        fun shouldCreateAndReturnStudyLogDailyMissions() {
            // given
            val dailyMissions = listOf(dailyMission1, dailyMission2)
            given(studyLogDailyMissionRepository.saveAll(anyList())).willAnswer { it.getArgument(0) }

            // when
            val result = studyLogDailyMissionCommandService.createStudyLogDailyMissions(studyLog, dailyMissions)

            // then
            assertThat(result).hasSize(dailyMissions.size)
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogDailyMissions 메서드는")
    inner class HardDeleteStudyLogDailyMissions {
        @Test
        @DisplayName("삭제된 학습 로그 데일리 미션이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDeletedStudyLogDailyMissionsDoNotExist() {
            // given
            given(studyLogDailyMissionCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L)

            // when
            val result = studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissions()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 학습 로그 데일리 미션이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDeletedStudyLogDailyMissionsExist() {
            // given
            given(studyLogDailyMissionCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L)

            // when
            val result = studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissions()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogDailyMissionsOwnedByDeletedDailyMission 메서드는")
    inner class HardDeleteStudyLogDailyMissionsOwnedByDeletedDailyMission {
        @Test
        @DisplayName("삭제된 데일리 미션이 소유한 학습 로그 데일리 미션이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenStudyLogDailyMissionsOwnedByDeletedDailyMissionDoNotExist() {
            // given
            given(studyLogDailyMissionCommandRepository.deleteAllByDeletedDailyMissionOwner()).willReturn(0L)

            // when
            val result = studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissionsOwnedByDeletedDailyMission()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 데일리 미션이 소유한 학습 로그 데일리 미션이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenStudyLogDailyMissionsOwnedByDeletedDailyMissionExist() {
            // given
            given(studyLogDailyMissionCommandRepository.deleteAllByDeletedDailyMissionOwner()).willReturn(5L)

            // when
            val result = studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissionsOwnedByDeletedDailyMission()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog 메서드는")
    inner class HardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog {
        @Test
        @DisplayName("삭제된 학습 로그가 소유한 학습 로그 데일리 미션이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenStudyLogDailyMissionsOwnedByDeletedStudyLogDoNotExist() {
            // given
            given(studyLogDailyMissionCommandRepository.deleteAllByDeletedStudyLogOwner()).willReturn(0L)

            // when
            val result = studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 학습 로그가 소유한 학습 로그 데일리 미션이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenStudyLogDailyMissionsOwnedByDeletedStudyLogExist() {
            // given
            given(studyLogDailyMissionCommandRepository.deleteAllByDeletedStudyLogOwner()).willReturn(5L)

            // when
            val result = studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogDailyMissionsOwnedByMember 메서드는")
    inner class HardDeleteStudyLogDailyMissionsOwnedByMember {
        @Test
        @DisplayName("특정 멤버가 소유한 학습 로그 데일리 미션이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenStudyLogDailyMissionsOwnedByMemberDoNotExist() {
            // given
            val memberId = member.id.requireId()
            given(studyLogDailyMissionCommandRepository.deleteAllByMemberId(memberId)).willReturn(0L)

            // when
            val result = studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissionsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 멤버가 소유한 학습 로그 데일리 미션이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenStudyLogDailyMissionsOwnedByMemberExist() {
            // given
            val memberId = member.id.requireId()
            given(studyLogDailyMissionCommandRepository.deleteAllByMemberId(memberId)).willReturn(5L)

            // when
            val result = studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissionsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(5L)
        }
    }
}
