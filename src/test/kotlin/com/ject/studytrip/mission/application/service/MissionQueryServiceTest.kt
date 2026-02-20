package com.ject.studytrip.mission.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.mission.domain.error.MissionErrorCode
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.mission.domain.repository.MissionQueryRepository
import com.ject.studytrip.mission.domain.repository.MissionRepository
import com.ject.studytrip.mission.fixture.MissionFixture
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.fixture.StampFixture
import com.ject.studytrip.trip.domain.model.TripCategory
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

@DisplayName("MissionQueryService 단위 테스트")
class MissionQueryServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var missionQueryService: MissionQueryService

    @Mock
    private lateinit var missionRepository: MissionRepository

    @Mock
    private lateinit var missionQueryRepository: MissionQueryRepository

    private lateinit var courseStamp: Stamp
    private lateinit var exploreStamp: Stamp
    private lateinit var courseMission: Mission
    private lateinit var exploreMission1: Mission
    private lateinit var exploreMission2: Mission

    @BeforeEach
    fun setUp() {
        val member = MemberFixture().createFromKakao()
        val courseTrip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
        val exploreTrip = TripFixture(member, TripCategory.EXPLORE).createWithId(2L)
        courseStamp = StampFixture(courseTrip, 1).createWithId(1L)
        exploreStamp = StampFixture(exploreTrip, 0).createWithId(2L)
        courseMission = MissionFixture(courseStamp).createWithId(1L)
        exploreMission1 = MissionFixture(exploreStamp).createWithId(2L)
        exploreMission2 = MissionFixture(exploreStamp).createWithId(3L)
    }

    @Nested
    @DisplayName("getValidMission 메서드는")
    inner class GetValidMission {
        @Test
        @DisplayName("미션이 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMissionDoesNotExist() {
            // given
            val missionId = -1L
            given(missionRepository.findById(missionId)).willReturn(Optional.empty())

            // when
            val exception = assertThrows<CustomException> { missionQueryService.getValidMission(courseStamp.id.requireId(), missionId) }

            // then
            assertThat(exception.message).isEqualTo(MissionErrorCode.MISSION_NOT_FOUND.message)
        }

        @Test
        @DisplayName("특정 미션이 다른 스템프에 속한다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMissionNotBelongToStamp() {
            // given
            val missionId = exploreMission1.id.requireId()
            given(missionRepository.findById(missionId)).willReturn(Optional.of(exploreMission1))

            // when
            val exception = assertThrows<CustomException> { missionQueryService.getValidMission(courseStamp.id.requireId(), missionId) }

            // then
            assertThat(exception.message).isEqualTo(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.message)
        }

        @Test
        @DisplayName("미션이 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMissionAlreadyDeleted() {
            // given
            val missionId = exploreMission1.id.requireId()
            exploreMission1.updateDeletedAt()
            given(missionRepository.findById(missionId)).willReturn(Optional.of(exploreMission1))

            // when
            val exception = assertThrows<CustomException> { missionQueryService.getValidMission(exploreStamp.id.requireId(), missionId) }

            // then
            assertThat(exception.message).isEqualTo(MissionErrorCode.MISSION_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("미션이 이미 완료되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMissionAlreadyCompleted() {
            // given
            val missionId = exploreMission1.id.requireId()
            exploreMission1.updateCompleted()
            given(missionRepository.findById(missionId)).willReturn(Optional.of(exploreMission1))

            // when
            val exception = assertThrows<CustomException> { missionQueryService.getValidMission(exploreStamp.id.requireId(), missionId) }

            // then
            assertThat(exception.message).isEqualTo(MissionErrorCode.MISSION_ALREADY_COMPLETED.message)
        }

        @Test
        @DisplayName("특정 스탬프에 속한 미션이 존재하면 미션을 조회하고 반환한다.")
        fun shouldReturnMissionWhenMissionBelongsToStamp() {
            // given
            val missionId = exploreMission1.id.requireId()
            given(missionRepository.findById(missionId)).willReturn(Optional.of(exploreMission1))

            // when
            val result = missionQueryService.getValidMission(exploreStamp.id.requireId(), missionId)

            // then
            assertThat(result).isEqualTo(exploreMission1)
        }
    }

    @Nested
    @DisplayName("getMissionsByStampId 메서드는")
    inner class GetMissionsByStampId {
        @Test
        @DisplayName("특정 스탬프에 속한 미션 목록을 최신순으로 정렬하여 반환한다.")
        fun shouldReturnMissionsByStampIdSortedByLatest() {
            // given
            val stampId = exploreStamp.id.requireId()
            given(
                missionRepository.findAllByStampIdAndDeletedAtIsNullOrderByCreatedAt(stampId),
            ).willReturn(listOf(exploreMission2, exploreMission1))

            // when
            val result = missionQueryService.getMissionsByStampId(stampId)

            // then
            assertThat(result).hasSize(2)
            assertThat(result).containsExactly(exploreMission2, exploreMission1)
        }
    }

    @Nested
    @DisplayName("getValidMissionsByIds 메서드는")
    inner class GetValidMissionsByIds {
        @Test
        @DisplayName("요청한 미션 ID 개수와 조회된 미션 개수가 일치하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenSomeMissionsDoNotExist() {
            // given
            val missionIds = listOf(exploreMission1.id.requireId(), exploreMission2.id.requireId(), 1000L)
            given(missionQueryRepository.findAllByIdsInFetchJoinStamp(missionIds)).willReturn(listOf(exploreMission1, exploreMission2))

            // when
            val exception = assertThrows<CustomException> { missionQueryService.getValidMissionsByIds(missionIds) }

            // then
            assertThat(exception.message).isEqualTo(MissionErrorCode.MISSION_NOT_FOUND.message)
        }

        @Test
        @DisplayName("미션이 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMissionAlreadyDeleted() {
            // given
            val missionIds = listOf(exploreMission1.id.requireId(), exploreMission2.id.requireId())
            exploreMission1.updateDeletedAt()
            given(missionQueryRepository.findAllByIdsInFetchJoinStamp(missionIds)).willReturn(listOf(exploreMission1, exploreMission2))

            // when
            val exception = assertThrows<CustomException> { missionQueryService.getValidMissionsByIds(missionIds) }

            // then
            assertThat(exception.message).isEqualTo(MissionErrorCode.MISSION_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("미션이 이미 완료되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMissionAlreadyCompleted() {
            // given
            val missionIds = listOf(exploreMission1.id.requireId(), exploreMission2.id.requireId())
            exploreMission1.updateCompleted()
            given(missionQueryRepository.findAllByIdsInFetchJoinStamp(missionIds)).willReturn(listOf(exploreMission1, exploreMission2))

            // when
            val exception = assertThrows<CustomException> { missionQueryService.getValidMissionsByIds(missionIds) }

            // then
            assertThat(exception.message).isEqualTo(MissionErrorCode.MISSION_ALREADY_COMPLETED.message)
        }

        @Test
        @DisplayName("미션 ID 목록과 일치하는 미션 목록을 조회하고 반환한다.")
        fun shouldReturnMissionsByIds() {
            // given
            val missionIds = listOf(exploreMission1.id.requireId(), exploreMission2.id.requireId())
            given(missionQueryRepository.findAllByIdsInFetchJoinStamp(missionIds)).willReturn(listOf(exploreMission1, exploreMission2))

            // when
            val result = missionQueryService.getValidMissionsByIds(missionIds)

            // then
            assertThat(result).hasSize(2)
            assertThat(result).contains(exploreMission1, exploreMission2)
        }
    }
}
