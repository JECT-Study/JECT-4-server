package com.ject.studytrip.dummy.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks

@DisplayName("DummyTripCommandService 단위 테스트")
class DummyTripCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var dummyTripCommandService: DummyTripCommandService

    private lateinit var member: Member

    @BeforeEach
    fun setUp() {
        member = MemberFixture.createMemberFromKakao()
    }

    companion object {
        private const val DUMMY_TRIP_COUNT = 10
    }

    @Nested
    @DisplayName("createDummyTrip 메서드는")
    inner class CreateDummyTrip {
        @Test
        @DisplayName("코스형 더미 여행을 생성하고 반환한다.")
        fun shouldReturnDummyCourseTripWhenCategoryIsCourse() {
            // given
            val category = "COURSE"

            // when
            val result = dummyTripCommandService.createDummyTrip(member, category, DUMMY_TRIP_COUNT)

            // then
            assertThat(result).isNotNull
            assertThat(result.category.name).isEqualTo(category)
            assertThat(result.endDate).isNotNull
        }

        @Test
        @DisplayName("탐험형 더미 여행을 생성하고 반환한다.")
        fun shouldReturnDummyExploreTripWhenCategoryIsExplore() {
            // given
            val category = "EXPLORE"

            // when
            val result = dummyTripCommandService.createDummyTrip(member, category, DUMMY_TRIP_COUNT)

            // then
            assertThat(result).isNotNull
            assertThat(result.category.name).isEqualTo(category)
            assertThat(result.endDate).isNull()
        }
    }
}
