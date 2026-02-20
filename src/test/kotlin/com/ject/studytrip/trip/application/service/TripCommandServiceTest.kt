package com.ject.studytrip.trip.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.trip.domain.error.TripErrorCode
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.domain.repository.TripCommandRepository
import com.ject.studytrip.trip.domain.repository.TripRepository
import com.ject.studytrip.trip.fixture.CreateTripRequestFixture
import com.ject.studytrip.trip.fixture.TripFixture
import com.ject.studytrip.trip.fixture.UpdateTripRequestFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import java.time.LocalDate

@DisplayName("TripCommandService 단위 테스트")
class TripCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var tripCommandService: TripCommandService

    @Mock
    private lateinit var tripRepository: TripRepository

    @Mock
    private lateinit var tripCommandRepository: TripCommandRepository

    private lateinit var member: Member
    private lateinit var courseTrip: Trip
    private lateinit var exploreTrip: Trip

    companion object {
        private const val NEW_TRIP_NAME = "새로운 여행 이름"
        private const val NEW_TRIP_MEMO = "새로운 여행 메모"
        private const val NEW_TRIP_CATEGORY = "EXPLORE"
        private val NEW_TRIP_END_DATE = LocalDate.now().plusDays(100)
    }

    @BeforeEach
    fun setUp() {
        member = MemberFixture().createFromKakaoWithId(1L)
        courseTrip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
        exploreTrip = TripFixture(member, TripCategory.EXPLORE).createWithId(2L)
    }

    @Nested
    @DisplayName("createTrip 메서드는")
    inner class CreateTrip {
        private val fixture = CreateTripRequestFixture()

        @Test
        @DisplayName("코스형 여행의 종료일이 null이면 예외가 발생한다.")
        fun shouldThrowExceptionWhenCourseTripEndDateIsNull() {
            // given
            val request = fixture.withCategory(TripCategory.COURSE.name).withEndDate(null).build()

            // when
            val exception = assertThrows<CustomException> { tripCommandService.createTrip(member, request) }

            // then
            assertThat(exception.message).isEqualTo(TripErrorCode.COURSE_TRIP_END_DATE_REQUIRED.message)
        }

        @Test
        @DisplayName("요청이 유효하면 코스형 여행을 생성하고 반환한다.")
        fun shouldCreateAndReturnCourseTripWhenRequestIsValid() {
            // given
            val request = fixture.withCategory(TripCategory.COURSE.name).build()
            given(tripRepository.save(any())).willReturn(courseTrip)

            // when
            val result = tripCommandService.createTrip(member, request)

            // then
            assertThat(result).isEqualTo(courseTrip)
            assertThat(result.category).isEqualTo(TripCategory.COURSE)
            assertThat(result.member).isEqualTo(member)
        }

        @Test
        @DisplayName("요청이 유효하면 탐험형 여행을 생성하고 반환한다.")
        fun shouldCreateAndReturnExploreTripWhenRequestIsValid() {
            // given
            val request = fixture.withCategory(TripCategory.EXPLORE.name).build()
            given(tripRepository.save(any())).willReturn(exploreTrip)

            // when
            val result = tripCommandService.createTrip(member, request)

            // then
            assertThat(result).isEqualTo(exploreTrip)
            assertThat(result.category).isEqualTo(TripCategory.EXPLORE)
            assertThat(result.member).isEqualTo(member)
        }
    }

    @Nested
    @DisplayName("updateTrip 메서드는")
    inner class UpdateTrip {
        private val fixture = UpdateTripRequestFixture()

        @Test
        @DisplayName("특정 여행의 이름을 수정한다.")
        fun shouldUpdateTripWhenNameIsPresent() {
            // given
            val request = fixture.withName(NEW_TRIP_NAME).build()
            val existingName = courseTrip.name

            // when
            tripCommandService.updateTrip(courseTrip, request)

            // then
            assertThat(courseTrip.name).isEqualTo(NEW_TRIP_NAME)
            assertThat(courseTrip.name).isNotEqualTo(existingName)
        }

        @Test
        @DisplayName("특정 여행의 메모를 수정한다.")
        fun shouldUpdateTripWhenMemoIsPresent() {
            // given
            val request = fixture.withMemo(NEW_TRIP_MEMO).build()
            val existingMemo = courseTrip.memo

            // when
            tripCommandService.updateTrip(courseTrip, request)

            // then
            assertThat(courseTrip.memo).isEqualTo(NEW_TRIP_MEMO)
            assertThat(courseTrip.memo).isNotEqualTo(existingMemo)
        }

        @Test
        @DisplayName("특정 여행의 카테고리를 수정한다.")
        fun shouldUpdateTripWhenCategoryIsPresent() {
            // given
            val request = fixture.withCategory(NEW_TRIP_CATEGORY).build()
            val existingCategory = courseTrip.category.name

            // when
            tripCommandService.updateTrip(courseTrip, request)

            // then
            assertThat(courseTrip.category.name).isEqualTo(NEW_TRIP_CATEGORY)
            assertThat(courseTrip.category.name).isNotEqualTo(existingCategory)
        }

        @Test
        @DisplayName("특정 여행의 카테고리를 수정한다.")
        fun shouldUpdateTripWhenEndDateIsPresent() {
            // given
            val request = fixture.withEndDate(NEW_TRIP_END_DATE).build()
            val existingEndDate = courseTrip.endDate

            // when
            tripCommandService.updateTrip(courseTrip, request)

            // then
            assertThat(courseTrip.endDate).isEqualTo(NEW_TRIP_END_DATE)
            assertThat(courseTrip.endDate).isNotEqualTo(existingEndDate)
        }
    }

    @Nested
    @DisplayName("deleteTrip 메서드는")
    inner class DeleteTrip {
        @Test
        @DisplayName("여행이 삭제될 때 deletedAt 필드를 현재 시간으로 업데이트한다. (소프트 삭제)")
        fun shouldUpdateDeletedAtWhenTripIsDeleted() {
            // when
            tripCommandService.deleteTrip(courseTrip)

            // then
            assertThat(courseTrip.deletedAt).isNotNull
        }
    }

    @Nested
    @DisplayName("completeTrip 메서드는")
    inner class CompleteTrip {
        @Test
        @DisplayName("미션이 완료될 때 completed 필드를 true로 업데이트한다.")
        fun shouldUpdateCompletedWhenTripIsCompleted() {
            // when
            tripCommandService.completeTrip(courseTrip)

            // then
            assertThat(courseTrip.isCompleted()).isTrue
        }
    }

    @Nested
    @DisplayName("increaseTotalStamps 메서드는")
    inner class IncreaseTotalStamps {
        @Test
        @DisplayName("여행의 총 스탬프 수를 +1 증가시킨다.")
        fun shouldIncreaseTotalStamps() {
            // given
            val existingTotalStamps = courseTrip.totalStamps

            // when
            tripCommandService.increaseTotalStamps(courseTrip)

            // then
            assertThat(courseTrip.totalStamps).isEqualTo(existingTotalStamps + 1)
        }
    }

    @Nested
    @DisplayName("decreaseTotalStamps 메서드는")
    inner class DecreaseTotalStamps {
        @Test
        @DisplayName("여행의 총 스탬프 수를 -1 감소시킨다.")
        fun shouldDecreaseTotalStamps() {
            // given
            val existingTotalStamps = courseTrip.totalStamps

            // when
            tripCommandService.decreaseTotalStamps(courseTrip)

            // then
            assertThat(courseTrip.totalStamps).isEqualTo(existingTotalStamps - 1)
        }
    }

    @Nested
    @DisplayName("increaseCompletedStamps 메서드는")
    inner class IncreaseCompletedStamps {
        @Test
        @DisplayName("여행의 완료된 총 스탬프 수를 +1 증가시킨다.")
        fun shouldIncreaseCompletedStamps() {
            // given
            val existingCompletedStamps = courseTrip.completedStamps

            // when
            tripCommandService.increaseCompletedStamps(courseTrip)

            // then
            assertThat(courseTrip.completedStamps).isEqualTo(existingCompletedStamps + 1)
        }
    }

    @Nested
    @DisplayName("hardDeleteTrips 메서드는")
    inner class HardDeleteTrips {
        @Test
        @DisplayName("삭제된 여행이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDeletedTripsDoNotExist() {
            // given
            given(tripCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L)

            // when
            val result = tripCommandService.hardDeleteTrips()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 여행이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDeletedTripsExist() {
            // given
            given(tripCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L)

            // when
            val result = tripCommandService.hardDeleteTrips()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteTripsOwnedByDeletedMember 메서드는")
    inner class HardDeleteTripsOwnedByDeletedMember {
        @Test
        @DisplayName("삭제된 멤버가 소유한 여행이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenTripsOwnedByDeletedMemberDoNotExist() {
            // given
            given(tripCommandRepository.deleteAllByDeletedMemberOwner()).willReturn(0L)

            // when
            val result = tripCommandService.hardDeleteTripsOwnedByDeletedMember()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 멤버가 소유한 여행이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenTripsOwnedByDeletedMemberExist() {
            // given
            given(tripCommandRepository.deleteAllByDeletedMemberOwner()).willReturn(5L)

            // when
            val result = tripCommandService.hardDeleteTripsOwnedByDeletedMember()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteTripsByMember 메서드는")
    inner class HardDeleteTripsByMember {
        @Test
        @DisplayName("특정 멤버가 소유한 여행이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenTripsOwnedByMemberDoNotExist() {
            // given
            val memberId = member.id.requireId()
            given(tripCommandRepository.deleteAllByMemberId(memberId)).willReturn(0L)

            // when
            val result = tripCommandService.hardDeleteTripsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 멤버가 소유한 여행이 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenTripsOwnedByMemberExist() {
            // given
            val memberId = member.id.requireId()
            given(tripCommandRepository.deleteAllByMemberId(memberId)).willReturn(5L)

            // when
            val result = tripCommandService.hardDeleteTripsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(5L)
        }
    }
}
