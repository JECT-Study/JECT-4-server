package com.ject.studytrip.mission.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.mission.domain.error.MissionErrorCode
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.mission.domain.repository.MissionCommandRepository
import com.ject.studytrip.mission.domain.repository.MissionRepository
import com.ject.studytrip.mission.fixture.CreateMissionRequestFixture
import com.ject.studytrip.mission.fixture.MissionFixture
import com.ject.studytrip.mission.fixture.UpdateMissionRequestFixture
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.fixture.StampFixture
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.fixture.TripFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any

@DisplayName("MissionCommandService 단위 테스트")
class MissionCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var missionCommandService: MissionCommandService

    @Mock
    private lateinit var missionRepository: MissionRepository

    @Mock
    private lateinit var missionCommandRepository: MissionCommandRepository

    private lateinit var member: Member
    private lateinit var courseStamp: Stamp
    private lateinit var exploreStamp: Stamp
    private lateinit var courseMission: Mission
    private lateinit var exploreMission1: Mission
    private lateinit var exploreMission2: Mission

    companion object {
        private const val NEW_MISSION_NAME = "새오운 미션 이름"
    }

    @BeforeEach
    fun setUp() {
        member = MemberFixture().createFromKakaoWithId(1L)
        val courseTrip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
        val exploreTrip = TripFixture(member, TripCategory.EXPLORE).createWithId(2L)
        courseStamp = StampFixture(courseTrip, 1).createWithId(1L)
        exploreStamp = StampFixture(exploreTrip, 0).createWithId(2L)
        courseMission = MissionFixture(courseStamp).createWithId(1L)
        exploreMission1 = MissionFixture(exploreStamp).createWithId(2L)
        exploreMission2 = MissionFixture(exploreStamp).createWithId(3L)
    }

    @Nested
    @DisplayName("createMission 메서드는")
    inner class CreateMission {
        private val fixture = CreateMissionRequestFixture()

        @Test
        @DisplayName("코스형 스탬프에 대한 미션을 생성하고 반환한다.")
        fun shouldCreateAndReturnMissionForCourseStamp() {
            // given
            val request = fixture.build()
            given(missionRepository.save(any())).willReturn(courseMission)

            // when
            val result = missionCommandService.createMission(courseStamp, request)

            // then
            assertThat(result).isEqualTo(courseMission)
            assertThat(result.stamp).isEqualTo(courseStamp)
        }

        @Test
        @DisplayName("탐험형 스템프에 대한 미션을 생성하고 반환한다.")
        fun shouldCreateAndReturnMissionForExploreStamp() {
            // given
            val request = fixture.build()
            given(missionRepository.save(any())).willReturn(exploreMission1)

            // when
            val result = missionCommandService.createMission(exploreStamp, request)

            // then
            assertThat(result).isEqualTo(exploreMission1)
            assertThat(result.stamp).isEqualTo(exploreStamp)
        }
    }

    @Nested
    @DisplayName("updateMissionNameIfPresent 메서드는")
    inner class UpdateMissionNameIfPresent {
        private val fixture = UpdateMissionRequestFixture()

        @Test
        @DisplayName("특정 미션의 이름을 수정한다.")
        fun shouldUpdateMissionWhenNameIsPresent() {
            // given
            val request = fixture.withName(NEW_MISSION_NAME).build()
            val existingName = courseMission.name

            // when
            missionCommandService.updateMissionNameIfPresent(courseMission, request)

            // then
            assertThat(courseMission.name).isEqualTo(NEW_MISSION_NAME)
            assertThat(courseMission.name).isNotEqualTo(existingName)
        }
    }

    @Nested
    @DisplayName("deleteMission 메서드는")
    inner class DeleteMission {
        @Test
        @DisplayName("미션이 삭제될 때 deletedAt 필드를 현재 시간으로 업데이트한다. (소프트 삭제)")
        fun shouldUpdateDeletedAtWhenMissionIsDeleted() {
            // when
            missionCommandService.deleteMission(courseMission)

            // then
            assertThat(courseMission.deletedAt).isNotNull
        }
    }

    @Nested
    @DisplayName("completeMission 메서드는")
    inner class CompleteMission {
        @Test
        @DisplayName("미션이 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMissionAlreadyDeleted() {
            // given
            exploreMission1.updateDeletedAt()

            // when
            val exception = assertThrows<CustomException> { missionCommandService.completeMission(exploreMission1) }

            // then
            assertThat(exception.message).isEqualTo(MissionErrorCode.MISSION_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("미션이 이미 완료되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMissionAlreadyCompleted() {
            // given
            exploreMission1.updateCompleted()

            // when
            val exception = assertThrows<CustomException> { missionCommandService.completeMission(exploreMission1) }

            // then
            assertThat(exception.message).isEqualTo(MissionErrorCode.MISSION_ALREADY_COMPLETED.message)
        }

        @Test
        @DisplayName("미션이 완료될 때 completed 필드를 true로 업데이트한다.")
        fun shouldUpdateCompletedWhenMissionIsCompleted() {
            // when
            missionCommandService.completeMission(exploreMission1)

            // then
            assertThat(exploreMission1.isCompleted).isTrue
        }
    }

    @Nested
    @DisplayName("validateMissionsBelongToStamp 메서드는")
    inner class ValidateMissionsBelongToStamp {
        @Test
        @DisplayName("특정 스탬프에 속하지 않은 미션이 하나라도 존재하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMissionsNotBelongToStamp() {
            // given
            val missions = listOf(courseMission, exploreMission2)

            // when
            val exception =
                assertThrows<CustomException> { missionCommandService.validateMissionsBelongToStamp(exploreStamp.id, missions) }

            // then
            assertThat(exception.message).isEqualTo(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.message)
        }

        @Test
        @DisplayName("모든 미션이 특정 스탬프에 속한다면 예외가 발생하지 않는다.")
        fun shouldPassWhenMissionsBelongsToStamp() {
            // given
            val missions = listOf(exploreMission1, exploreMission2)

            // when & then
            assertDoesNotThrow { missionCommandService.validateMissionsBelongToStamp(exploreStamp.id, missions) }
        }
    }

    @Nested
    @DisplayName("validateAllMissionsCompletedByStampId 메서드는")
    inner class ValidateAllMissionsCompletedByStampId {
        @Test
        @DisplayName("특정 스탬프의 어떤 미션이 완료되지 않았다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenAnyMissionIsNotCompleted() {
            // given
            val stampId = courseStamp.id
            given(missionCommandRepository.existsByStampIdAndCompletedIsFalseAndDeletedAtIsNull(stampId)).willReturn(true)

            // when
            val exception = assertThrows<CustomException> { missionCommandService.validateAllMissionsCompletedByStampId(stampId) }

            // then
            assertThat(exception.message).isEqualTo(MissionErrorCode.ALL_MISSIONS_NOT_COMPLETED.message)
        }

        @Test
        @DisplayName("특정 스탬프의 모든 미션이 완료되었다면 예외가 발생하지 않는다.")
        fun shouldPassWhenAllMissionsAreCompleted() {
            // given
            val stampId = courseStamp.id
            given(missionCommandRepository.existsByStampIdAndCompletedIsFalseAndDeletedAtIsNull(stampId)).willReturn(false)

            // when & then
            assertDoesNotThrow { missionCommandService.validateAllMissionsCompletedByStampId(stampId) }
        }
    }

    @Nested
    @DisplayName("hardDeleteMissions 메서드는")
    inner class HardDeleteMissions {
        @Test
        @DisplayName("삭제된 미션이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDeletedMissionsDoNotExist() {
            // given
            given(missionCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L)

            // when
            val result = missionCommandService.hardDeleteMissions()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 미션이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDeletedMissionsExist() {
            // given
            given(missionCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L)

            // when
            val result = missionCommandService.hardDeleteMissions()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteMissionsOwnedByDeletedStamp 메서드는")
    inner class HardDeleteMissionsOwnedByDeletedStamp {
        @Test
        @DisplayName("삭제된 스탬프가 소유한 미션이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenMissionsOwnedByDeletedStampDoNotExist() {
            // given
            given(missionCommandRepository.deleteAllByDeletedStampOwner()).willReturn(0L)

            // when
            val result = missionCommandService.hardDeleteMissionsOwnedByDeletedStamp()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 스탬프가 소유한 미션이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenMissionsOwnedByDeletedStampExist() {
            // given
            given(missionCommandRepository.deleteAllByDeletedStampOwner()).willReturn(5L)

            // when
            val result = missionCommandService.hardDeleteMissionsOwnedByDeletedStamp()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteMissionsOwnedByMember 메서드는")
    inner class HardDeleteMissionsOwnedByMember {
        @Test
        @DisplayName("특정 멤버가 소유한 미션이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenMissionsOwnedByMemberDoNotExist() {
            // given
            val memberId = member.id
            given(missionCommandRepository.deleteAllByMemberId(memberId)).willReturn(0L)

            // when
            val result = missionCommandService.hardDeleteMissionsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 멤버가 소유한 미션이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenMissionsOwnedByMemberExist() {
            // given
            val memberId = member.id
            given(missionCommandRepository.deleteAllByMemberId(memberId)).willReturn(5L)

            // when
            val result = missionCommandService.hardDeleteMissionsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(5L)
        }
    }
}
