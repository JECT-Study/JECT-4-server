package com.ject.studytrip.dummy.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.fixture.StampFixture
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.fixture.TripFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks

@DisplayName("DummyMissionCommandService 단위 테스트")
class DummyMissionCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var dummyMissionCommandService: DummyMissionCommandService

    private lateinit var courseStamp: Stamp
    private lateinit var exploreStamp: Stamp

    @BeforeEach
    fun setUp() {
        val member = MemberFixture.createMemberFromKakao()
        val courseTrip = TripFixture(member, TripCategory.COURSE).create()
        val exploreTrip = TripFixture(member, TripCategory.EXPLORE).create()
        courseStamp = StampFixture(courseTrip, DUMMY_STAMP_COUNT).create()
        exploreStamp = StampFixture(exploreTrip, 0).create()
    }

    companion object {
        private const val DUMMY_STAMP_COUNT = 10
    }

    @Nested
    @DisplayName("createDummyMission 메서드는")
    inner class CreateDummyMission {
        @Test
        @DisplayName("코스형 스탬프에 대한 더미 미션을 생성하고 반환한다.")
        fun shouldReturnDummyMissionForCourseStamp() {
            // when
            val result = dummyMissionCommandService.createDummyMission(courseStamp)

            // then
            assertThat(result).isNotNull
            assertThat(result.stamp.stampOrder).isPositive
        }

        @Test
        @DisplayName("탐혐형 스탬프에 대한 더미 미션을 생성하고 반환한다.")
        fun shouldReturnDummyMissionForExploreStamp() {
            // when
            val result = dummyMissionCommandService.createDummyMission(exploreStamp)

            // then
            assertThat(result).isNotNull
            assertThat(result.stamp.stampOrder).isZero
        }
    }
}
