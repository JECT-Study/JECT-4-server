package com.ject.studytrip.stamp.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.stamp.domain.error.StampErrorCode
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.domain.repository.StampCommandRepository
import com.ject.studytrip.stamp.domain.repository.StampRepository
import com.ject.studytrip.stamp.fixture.CreateStampRequestFixture
import com.ject.studytrip.stamp.fixture.StampFixture
import com.ject.studytrip.stamp.fixture.UpdateStampOrderRequestFixture
import com.ject.studytrip.stamp.fixture.UpdateStampRequestFixture
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.fixture.TripFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyList
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import java.time.LocalDate

@DisplayName("StampCommandService 단위 테스트")
class StampCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var stampCommandService: StampCommandService

    @Mock
    private lateinit var stampRepository: StampRepository

    @Mock
    private lateinit var stampCommandRepository: StampCommandRepository

    private lateinit var member: Member
    private lateinit var courseTrip: Trip
    private lateinit var exploreTrip: Trip
    private lateinit var courseStamp1: Stamp
    private lateinit var courseStamp2: Stamp
    private lateinit var exploreStamp1: Stamp
    private lateinit var exploreStamp2: Stamp

    @BeforeEach
    fun setUp() {
        member = MemberFixture().createFromKakaoWithId(1L)
        courseTrip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
        exploreTrip = TripFixture(member, TripCategory.EXPLORE).createWithId(2L)
        courseStamp1 = StampFixture(courseTrip, 1).createWithId(1L)
        courseStamp2 = StampFixture(courseTrip, 2).createWithId(2L)
        exploreStamp1 = StampFixture(exploreTrip, 0).createWithId(3L)
        exploreStamp2 = StampFixture(exploreTrip, 0).createWithId(4L)
    }

    @Nested
    @DisplayName("createStamp 메서드는")
    inner class CreateStamp {
        private val fixture = CreateStampRequestFixture()

        @Test
        @DisplayName("스탬프 종료일이 여행 종료일보다 이후라면 예외가 발생한다.")
        fun shouldThrowExceptionWhenEndDateIsAfterTripEndDate() {
            // given
            val nextOrder = courseStamp2.stampOrder + 1
            val request = fixture.withEndDateAfterTripEndDate().build()

            // when
            val exception = assertThrows<CustomException> { stampCommandService.createStamp(courseTrip, nextOrder, request) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.STAMP_END_DATE_AFTER_TRIP_END_DATE_NOT_ALLOWED.message)
        }

        @Test
        @DisplayName("코스형 여행에 대한 스탬프를 생성하고 반환한다.")
        fun shouldCreateAndReturnStampWithNextOrderForCourseTrip() {
            // given
            val nextOrder = courseStamp1.stampOrder + 1
            val request = fixture.build()
            given(stampRepository.save(any())).willReturn(courseStamp2)

            // when
            val result = stampCommandService.createStamp(courseTrip, nextOrder, request)

            // then
            assertThat(result).isEqualTo(courseStamp2)
            assertThat(result.stampOrder).isEqualTo(nextOrder)
        }

        @Test
        @DisplayName("탐험형 여행에 대한 스탬프를 생성하고 반환한다.")
        fun shouldCreateAndReturnStampWithZeroOrderForExploreTrip() {
            // given
            val request = fixture.withEndDate(null).build()
            given(stampRepository.save(any())).willReturn(exploreStamp2)

            // when
            val result = stampCommandService.createStamp(exploreTrip, 0, request)

            // then
            assertThat(result).isEqualTo(exploreStamp2)
            assertThat(result.stampOrder).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("createStamps 메서드는")
    inner class CreateStamps {
        private val fixture = CreateStampRequestFixture()

        @Test
        @DisplayName("코스형 여행에 대한 스탬프 목록을 생성하고 반환한다.")
        fun shouldCreateAndReturnStampsWithNextOrderForCourseTrip() {
            // given
            val requests = listOf(fixture.build(), fixture.build())
            given(stampRepository.saveAll(anyList())).willReturn(listOf(courseStamp1, courseStamp2))

            // when
            stampCommandService.createStamps(courseTrip, 1, requests)

            // then
            verify(stampRepository).saveAll(anyList())
        }

        @Test
        @DisplayName("탐험형 여행에 대한 스탬프 목록을 생성하고 반환한다.")
        fun shouldCreateAndReturnStampsWithZeroOrderForExploreTrip() {
            // given
            val requests = listOf(fixture.withEndDate(null).build(), fixture.withEndDate(null).build())
            given(stampRepository.saveAll(anyList())).willReturn(listOf(exploreStamp1, exploreStamp2))

            // when
            stampCommandService.createStamps(exploreTrip, 0, requests)

            // then
            verify(stampRepository).saveAll(anyList())
        }
    }

    @Nested
    @DisplayName("updateStamp 메서드는")
    inner class UpdateStamp {
        private val fixture = UpdateStampRequestFixture()

        @Test
        @DisplayName("스탬프 종료일을 여행 종료일보다 이후 날짜로 수정하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenEndDateIsAfterTripEndDate() {
            // given
            val request = fixture.withEndDate(LocalDate.now().plusDays(100)).build()

            // when
            val exception = assertThrows<CustomException> { stampCommandService.updateStamp(courseTrip, courseStamp1, request) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.STAMP_END_DATE_AFTER_TRIP_END_DATE_NOT_ALLOWED.message)
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 스탬프 이름을 수정한다.")
        fun shouldUpdateStampNameWhenRequestIsValid() {
            // given
            val request = fixture.withEndDate(null).build()

            // when
            stampCommandService.updateStamp(courseTrip, courseStamp1, request)

            // then
            assertThat(courseStamp1.name).isEqualTo(request.name)
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 스탬프 종료일을 수정한다.")
        fun shouldUpdateStampEndDateWhenRequestIsValid() {
            // given
            val request = fixture.withName(null).build()

            // when
            stampCommandService.updateStamp(courseTrip, courseStamp1, request)

            // then
            assertThat(courseStamp1.endDate).isEqualTo(request.endDate)
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 스탬프 이름과 종료일을 수정한다.")
        fun shouldUpdateStampNameAndEndDateWhenRequestIsValid() {
            // given
            val request = fixture.build()

            // when
            stampCommandService.updateStamp(courseTrip, courseStamp1, request)

            // then
            assertThat(courseStamp1.name).isEqualTo(request.name)
            assertThat(courseStamp1.endDate).isEqualTo(request.endDate)
        }
    }

    @Nested
    @DisplayName("updateStampOrders 메서드는")
    inner class UpdateStampOrders {
        private val fixture = UpdateStampOrderRequestFixture()

        @Test
        @DisplayName("탐험형 여행의 스탬프 순서를 수정하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenTripIsExploreType() {
            // given
            val request = fixture.build()

            // when
            val exception = assertThrows<CustomException> { stampCommandService.updateStampOrders(exploreTrip, request) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.CANNOT_UPDATE_ORDER_FOR_EXPLORATION_TRIP.message)
        }

        @Test
        @DisplayName("요청에 존재하지 않는 스탬프 ID가 포함되어 있다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenRequestIsInvalid() {
            // given
            val request = fixture.withOrderedStampIds(listOf(1000L, 1001L)).build()

            // when
            val exception = assertThrows<CustomException> { stampCommandService.updateStampOrders(courseTrip, request) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.INVALID_STAMP_ID_IN_REQUEST.message)
        }

        @Test
        @DisplayName("특정 여행에 속하지 않은 스탬프가 하나라도 존재하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenStampsNotBelongToTrip() {
            // given
            val request = fixture.build()
            val newTrip = TripFixture(member, TripCategory.COURSE).createWithId(3L)
            given(stampRepository.findAllByIdIn(request.orderedStampIds)).willReturn(listOf(courseStamp1, courseStamp2))

            // when
            val exception = assertThrows<CustomException> { stampCommandService.updateStampOrders(newTrip, request) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.message)
        }

        @Test
        @DisplayName("스탬프가 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenStampAlreadyDeleted() {
            // given
            val request = fixture.build()
            courseStamp1.updateDeletedAt()
            given(stampRepository.findAllByIdIn(request.orderedStampIds)).willReturn(listOf(courseStamp1, courseStamp2))

            // when
            val exception = assertThrows<CustomException> { stampCommandService.updateStampOrders(courseTrip, request) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.STAMP_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("스탬프가 이미 완료되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenStampAlreadyCompleted() {
            // given
            val request = fixture.build()
            courseStamp1.updateCompleted()
            given(stampRepository.findAllByIdIn(request.orderedStampIds)).willReturn(listOf(courseStamp1, courseStamp2))

            // when
            val exception = assertThrows<CustomException> { stampCommandService.updateStampOrders(courseTrip, request) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.STAMP_ALREADY_COMPLETED.message)
        }

        @Test
        @DisplayName("코스형 여행에서 요청으로 들어온 스탬프 ID 리스트 순서에 따라 스탬프 순서를 수정한다.")
        fun shouldUpdateStampOrdersForCourseTrip() {
            // given
            val request = fixture.withOrderedStampIds(listOf(2L, 1L)).build()
            given(stampRepository.findAllByIdIn(request.orderedStampIds)).willReturn(listOf(courseStamp1, courseStamp2))

            // when
            stampCommandService.updateStampOrders(courseTrip, request)

            // then
            assertThat(courseStamp2.stampOrder).isEqualTo(1)
            assertThat(courseStamp1.stampOrder).isEqualTo(2)
        }
    }

    @Nested
    @DisplayName("updateStampOrdersByTripCategoryChange 메서드는")
    inner class UpdateStampOrdersByTripCategoryChange {
        @Test
        @DisplayName("여행 카테고리가 탐험형으로 변경되면 소속되어 있던 모든 스탬프 순서를 0으로 초기화한다.")
        fun shouldResetStampOrdersWhenTripCategoryChangesToExplore() {
            // given
            val tripId = courseTrip.id.requireId()
            given(stampRepository.findAllByTripIdOrderByCreatedAtAsc(tripId)).willReturn(listOf(courseStamp1, courseStamp2))

            // when
            stampCommandService.updateStampOrdersByTripCategoryChange(tripId, TripCategory.EXPLORE)

            // then
            assertThat(courseStamp1.stampOrder).isEqualTo(0)
            assertThat(courseStamp2.stampOrder).isEqualTo(0)
        }

        @Test
        @DisplayName("여행 카테고리가 코스형으로 변경되면 소속되어 있던 모든 스탬프 순서를 생성일 기준으로 1부터 초기화합니다.")
        fun shouldSetStampOrdersWhenCategoryChangesToCourse() {
            // given
            val tripId = exploreTrip.id.requireId()
            given(stampRepository.findAllByTripIdOrderByCreatedAtAsc(tripId)).willReturn(listOf(exploreStamp1, exploreStamp2))

            // when
            stampCommandService.updateStampOrdersByTripCategoryChange(tripId, TripCategory.COURSE)

            // then
            assertThat(exploreStamp1.stampOrder).isEqualTo(1)
            assertThat(exploreStamp2.stampOrder).isEqualTo(2)
        }
    }

    @Nested
    @DisplayName("deleteStamp 메서드는")
    inner class DeleteStamp {
        @Test
        @DisplayName("스탬프가 삭제될 때 deletedAt 필드를 현재 시간으로 업데이트한다. (소프트 삭제)")
        fun shouldUpdateDeletedAtWhenStampIsDeleted() {
            // when
            stampCommandService.deleteStamp(courseStamp1)

            // then
            assertThat(courseStamp1.deletedAt).isNotNull
        }
    }

    @Nested
    @DisplayName("completeStamp 메서드는")
    inner class CompleteStamp {
        @Test
        @DisplayName("스탬프가 완료될 때 completed 필드를 true로 업데이트한다.")
        fun shouldUpdateCompletedWhenStampIsCompleted() {
            // when
            stampCommandService.completeStamp(courseStamp1)

            // then
            assertThat(courseStamp1.isCompleted()).isTrue
        }
    }

    @Nested
    @DisplayName("shiftStampOrders 메서드는")
    inner class ShiftStampOrders {
        @Test
        @DisplayName("시프트할 스탬프가 존재하지 않으면 기존 스탬프 순서를 변경하지 않는다.")
        fun shouldNotChangeStampOrdersWhenStampsToShiftDoNotExist() {
            // when
            stampCommandService.shiftStampOrders(listOf())

            // then
            assertThat(courseStamp2.stampOrder).isEqualTo(2)
        }

        @Test
        @DisplayName("시프트할 스탬프가 존재하면 각 스탬프 순서를 1씩 감소시킨다.")
        fun shouldDecreaseStampOrdersByOneWhenStampsToShiftExist() {
            // when
            stampCommandService.shiftStampOrders(listOf(courseStamp2))

            // then
            assertThat(courseStamp2.stampOrder).isEqualTo(1)
        }
    }

    @Nested
    @DisplayName("increaseTotalMissions 메서드는")
    inner class IncreaseTotalMissions {
        @Test
        @DisplayName("스탬프의 총 미션 수를 증가시킨다.")
        fun shouldIncreaseTotalMissions() {
            // given
            val existingTotalMissions = courseStamp1.totalMissions

            // when
            stampCommandService.increaseTotalMissions(courseStamp1)

            // then
            assertThat(courseStamp1.totalMissions).isEqualTo(existingTotalMissions + 1)
        }

        @Test
        @DisplayName("스탬프의 총 미션 수를 1 감소시킨다.")
        fun shouldDecreaseTotalMissions() {
            // given
            courseStamp1.increaseTotalMissions()
            val existingTotalMissions = courseStamp1.totalMissions

            // when
            stampCommandService.decreaseTotalMissions(courseStamp1)

            // then
            assertThat(courseStamp1.totalMissions).isEqualTo(existingTotalMissions - 1)
        }
    }

    @Nested
    @DisplayName("increaseCompletedMissions 메서드는")
    inner class IncreaseCompletedMissions {
        @Test
        @DisplayName("지정된 개수만큼 스탬프의 완료된 미션 수를 증가시킨다.")
        fun shouldIncreaseCompletedMissions() {
            // given
            val existingCompletedMissions = courseStamp1.completedMissions
            val count = 2

            // when
            stampCommandService.increaseCompletedMissions(courseStamp1, count)

            // then
            assertThat(courseStamp1.completedMissions).isEqualTo(existingCompletedMissions + count)
        }
    }

    @Nested
    @DisplayName("validateStampBelongsToTrip 메서드는")
    inner class ValidateStampBelongsToTrip {
        @Test
        @DisplayName("특정 여행에 속하지 않은 스탬프가 존재하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenStampNotBelongToTrip() {
            // given
            val newTrip = TripFixture(member, TripCategory.COURSE).createWithId(3L)

            // when
            val exception =
                assertThrows<CustomException> { stampCommandService.validateStampBelongsToTrip(newTrip.id.requireId(), courseStamp1) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.message)
        }
    }

    @Nested
    @DisplayName("validateAllStampsCompletedByTripId 메서드는")
    inner class ValidateAllStampsCompletedByTripId {
        @Test
        @DisplayName("특정 여행의 어떤 스탬프가 완료되지 않았다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenAnyStampIsNotCompleted() {
            // given
            val tripId = courseTrip.id.requireId()
            given(stampCommandRepository.existsByTripIdAndCompletedIsFalseAndDeletedAtIsNull(tripId)).willReturn(true)

            // when
            val exception = assertThrows<CustomException> { stampCommandService.validateAllStampsCompletedByTripId(tripId) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.ALL_STAMPS_NOT_COMPLETED.message)
        }

        @Test
        @DisplayName("특정 여행의 모든 스탬프가 완료되었다면 예외가 발생하지 않는다.")
        fun shouldPassWhenAllStampsAreCompleted() {
            // given
            val tripId = courseTrip.id.requireId()
            given(stampCommandRepository.existsByTripIdAndCompletedIsFalseAndDeletedAtIsNull(tripId)).willReturn(false)

            // when & then
            assertDoesNotThrow { stampCommandService.validateAllStampsCompletedByTripId(tripId) }
        }
    }

    @Nested
    @DisplayName("hardDeleteStamps 메서드는")
    inner class HardDeleteStamps {
        @Test
        @DisplayName("삭제된 스탬프가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDeletedStampsDoNotExist() {
            // given
            given(stampCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L)

            // when
            val result = stampCommandService.hardDeleteStamps()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 스탬프가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDeletedStampsExist() {
            // given
            given(stampCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L)

            // when
            val result = stampCommandService.hardDeleteStamps()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteStampsOwnedByDeletedTrip 메서드는")
    inner class HardDeleteStampsOwnedByDeletedTrip {
        @Test
        @DisplayName("삭제된 여행이 소유한 스탬프가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenStampsOwnedByDeletedTripDoNotExist() {
            // given
            given(stampCommandRepository.deleteAllByDeletedTripOwner()).willReturn(0L)

            // when
            val result = stampCommandService.hardDeleteStampsOwnedByDeletedTrip()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 여행이 소유한 스탬프가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenStampsOwnedByDeletedStampExist() {
            // given
            given(stampCommandRepository.deleteAllByDeletedTripOwner()).willReturn(5L)

            // when
            val result = stampCommandService.hardDeleteStampsOwnedByDeletedTrip()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteStampsOwnedByMember 메서드는")
    inner class HardDeleteStampsOwnedByMember {
        @Test
        @DisplayName("특정 멤버가 소유한 스탬프가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenStampsOwnedByMemberDoNotExist() {
            // given
            val memberId = member.id.requireId()
            given(stampCommandRepository.deleteAllByMemberId(memberId)).willReturn(0L)

            // when
            val result = stampCommandService.hardDeleteStampsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 멤버가 소유한 스탬프가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenStampsOwnedByMemberExist() {
            // given
            val memberId = member.id.requireId()
            given(stampCommandRepository.deleteAllByMemberId(memberId)).willReturn(5L)

            // when
            val result = stampCommandService.hardDeleteStampsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(5L)
        }
    }
}
