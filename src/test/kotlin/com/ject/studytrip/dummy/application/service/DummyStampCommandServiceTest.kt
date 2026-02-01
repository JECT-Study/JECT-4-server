package com.ject.studytrip.dummy.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.fixture.TripFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks

@DisplayName("DummyStampCommandService 단위 테스트")
class DummyStampCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var dummyStampCommandService: DummyStampCommandService

    private lateinit var courseTrip: Trip
    private lateinit var exploreTrip: Trip

    @BeforeEach
    fun setUp() {
        val member = MemberFixture.createMemberFromKakao()
        courseTrip = TripFixture(member, TripCategory.COURSE).create()
        exploreTrip = TripFixture(member, TripCategory.EXPLORE).create()
    }

    companion object {
        private const val DUMMY_STAMP_COUNT = 10
    }

    @Test
    @DisplayName("코스형 여행에 대한 더미 스탬프를 생성하고 반환한다.")
    fun shouldReturnDummyStampForCourseTrip() {
        // when
        val result = dummyStampCommandService.createDummyStamp(courseTrip, DUMMY_STAMP_COUNT)

        // then
        assertThat(result).isNotNull
        assertThat(result.stampOrder).isPositive
        assertThat(result.endDate).isNotNull
    }

    @Test
    @DisplayName("탐험형 여행에 대한 더미 스탬프를 생성하고 반환한다.")
    fun shouldReturnDummyStampForExploreTrip() {
        // when
        val result = dummyStampCommandService.createDummyStamp(exploreTrip, DUMMY_STAMP_COUNT)

        // then
        assertThat(result).isNotNull
        assertThat(result.stampOrder).isZero
        assertThat(result.endDate).isNull()
    }
}
