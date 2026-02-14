package com.ject.studytrip.stamp.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.stamp.domain.error.StampErrorCode
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.domain.repository.StampQueryRepository
import com.ject.studytrip.stamp.domain.repository.StampRepository
import com.ject.studytrip.stamp.fixture.StampFixture
import com.ject.studytrip.trip.domain.model.Trip
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
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.Optional

@DisplayName("StampQueryService 단위 테스트")
class StampQueryServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var stampQueryService: StampQueryService

    @Mock
    private lateinit var stampRepository: StampRepository

    @Mock
    private lateinit var stampQueryRepository: StampQueryRepository

    private lateinit var courseTrip: Trip
    private lateinit var exploreTrip: Trip
    private lateinit var courseStamp1: Stamp
    private lateinit var courseStamp2: Stamp
    private lateinit var exploreStamp1: Stamp
    private lateinit var exploreStamp2: Stamp

    @BeforeEach
    fun setUp() {
        val member = MemberFixture().createFromKakaoWithId(1L)
        courseTrip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
        exploreTrip = TripFixture(member, TripCategory.EXPLORE).createWithId(2L)
        courseStamp1 = StampFixture(courseTrip, 1).createWithId(1L)
        courseStamp2 = StampFixture(courseTrip, 2).createWithId(2L)
        exploreStamp1 = StampFixture(exploreTrip, 0).createWithId(3L)
        exploreStamp2 = StampFixture(exploreTrip, 0).createWithId(4L)
    }

    @Nested
    @DisplayName("getValidStamp 메서드는")
    inner class GetValidStamp {
        @Test
        @DisplayName("스탬프가 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenStampDoesNotExist() {
            // given
            val stampId = -1L

            // when
            val exception = assertThrows<CustomException> { stampQueryService.getValidStamp(courseTrip.id, stampId) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.STAMP_NOT_FOUND.message)
        }

        @Test
        @DisplayName("특정 스탬프가 다른 여행에 속한다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenStampNotBelongToTrip() {
            // given
            val stampId = courseStamp1.id
            given(stampRepository.findById(stampId)).willReturn(Optional.of(courseStamp1))

            // when
            val exception = assertThrows<CustomException> { stampQueryService.getValidStamp(exploreTrip.id, stampId) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP.message)
        }

        @Test
        @DisplayName("스탬프가 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenStampAlreadyDeleted() {
            // given
            val stampId = courseStamp1.id
            courseStamp1.updateDeletedAt()
            given(stampRepository.findById(stampId)).willReturn(Optional.of(courseStamp1))

            // when
            val exception = assertThrows<CustomException> { stampQueryService.getValidStamp(courseTrip.id, stampId) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.STAMP_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("스탬프가 이미 완료되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenStampAlreadyCompleted() {
            // given
            val stampId = courseStamp1.id
            courseStamp1.updateCompleted()
            given(stampRepository.findById(stampId)).willReturn(Optional.of(courseStamp1))

            // when
            val exception = assertThrows<CustomException> { stampQueryService.getValidStamp(courseTrip.id, stampId) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.STAMP_ALREADY_COMPLETED.message)
        }

        @Test
        @DisplayName("특정 여행에 속한 스탬프가 존재하면 스탬프를 조회하고 반환한다.")
        fun shouldReturnStampWhenStampBelongsToTrip() {
            // given
            val stampId = courseStamp1.id
            given(stampRepository.findById(stampId)).willReturn(Optional.of(courseStamp1))

            // when
            val result = stampQueryService.getValidStamp(courseTrip.id, stampId)

            // then
            assertThat(result).isEqualTo(courseStamp1)
        }
    }

    @Nested
    @DisplayName("getStampsByTripId 메서드는")
    inner class GetStampsByTripId {
        @Test
        @DisplayName("특정 여행에 속한 삭제되지 않은 스탬프 목록을 조회하고 반환한다.")
        fun shouldReturnStampsByTripIdAndDeletedAtIsNull() {
            // given
            val tripId = courseTrip.id
            given(stampRepository.findAllByTripIdAndDeletedAtIsNull(tripId)).willReturn(listOf(courseStamp1, courseStamp2))

            // when
            val result = stampQueryService.getStampsByTripId(tripId)

            // then
            assertThat(result).hasSize(2)
            assertThat(result).containsExactly(courseStamp1, courseStamp2)
        }
    }

    @Nested
    @DisplayName("getFirstInProcessingStampsForCourseTrip 메서드는")
    inner class GetFirstInProcessingStampsForCourseTrip {
        @Test
        @DisplayName("특정 코스형 여행에서 진행 중인 스탬프가 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenProgressStampDoesNotExistForCourseTrip() {
            // given
            val tripId = courseTrip.id
            courseStamp1.updateCompleted()
            courseStamp2.updateCompleted()
            given(stampQueryRepository.findFirstIncompleteStampByTripId(tripId)).willReturn(Optional.empty())

            // when
            val exception = assertThrows<CustomException> { stampQueryService.getFirstInProcessingStampsForCourseTrip(tripId) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.STAMP_NOT_FOUND.message)
        }

        @Test
        @DisplayName("특정 코스형 여행에서 진행 중인 첫번째 스탬프를 조회하고 반환한다.")
        fun shouldReturnFirstProcessingStampForCourseTrip() {
            // given
            val tripId = courseTrip.id
            given(stampQueryRepository.findFirstIncompleteStampByTripId(tripId)).willReturn(Optional.of(courseStamp1))

            // when
            val result = stampQueryService.getFirstInProcessingStampsForCourseTrip(tripId)

            // then
            assertThat(result).isEqualTo(courseStamp1)
            assertThat(result.isDeleted).isFalse
            assertThat(result.isCompleted).isFalse
        }
    }

    @Nested
    @DisplayName("getStampNameByTripCategory 메서드는")
    inner class GetStampNameByTripCategory {
        @Test
        @DisplayName("스탬프 목록이 비어있다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenStampListIsEmpty() {
            // given
            val stamps = emptyList<Stamp>()

            // when
            val exception = assertThrows<CustomException> { stampQueryService.getStampNameByTripCategory(TripCategory.COURSE, stamps) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.STAMP_LIST_NOT_EMPTY.message)
        }

        @Test
        @DisplayName("스탬프가 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenStampAlreadyDeleted() {
            // given
            courseStamp1.updateDeletedAt()
            val stamps = listOf(courseStamp1)
            // when
            val exception = assertThrows<CustomException> { stampQueryService.getStampNameByTripCategory(TripCategory.COURSE, stamps) }

            // then
            assertThat(exception.message).isEqualTo(StampErrorCode.STAMP_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("코스형 여행일 경우, 스탬프 리스트에서 첫번째 스탬프 이름을 반환한다.")
        fun shouldReturnFirstStampNameForCourseTrip() {
            // given
            val stamps = listOf(courseStamp1, courseStamp2)

            // when
            val result = stampQueryService.getStampNameByTripCategory(TripCategory.COURSE, stamps)

            // then
            assertThat(result).isEqualTo(courseStamp1.name)
        }

        @Test
        @DisplayName("탐험형 여행일 경우, 선택한 미션들의 스탬프 중 가장 많이 포함된 스탬프 이름을 반환한다.")
        fun shouldReturnMostFrequentStampNameForExploreTrip() {
            // given
            val stamps = listOf(exploreStamp1, exploreStamp2, exploreStamp2, exploreStamp2)

            // when
            val result = stampQueryService.getStampNameByTripCategory(TripCategory.EXPLORE, stamps)

            // then
            assertThat(result).isEqualTo(exploreStamp2.name)
        }

        @Test
        @DisplayName("탐험형 여행일 경우, 가장 많이 포함된 스탬프가 2개 이상이라면 생성일이 가장 빠른 스탬프 이름을 반환한다.")
        fun shouldReturnEarliestStampNameWhenMostFrequentStampsAreMultiple() {
            // given
            ReflectionTestUtils.setField(exploreStamp1, "createdAt", LocalDateTime.now())
            ReflectionTestUtils.setField(exploreStamp2, "createdAt", LocalDateTime.now().minusDays(1))
            val stamps = listOf(exploreStamp1, exploreStamp1, exploreStamp2, exploreStamp2)

            // when
            val result = stampQueryService.getStampNameByTripCategory(TripCategory.EXPLORE, stamps)

            // then
            assertThat(result).isEqualTo(exploreStamp2.name)
        }
    }

    @Nested
    @DisplayName("getNextStampOrderByTrip 메서드는")
    inner class GetNextStampOrderByTrip {
        @Test
        @DisplayName("탐험형 여행이라면 0을 반환한다.")
        fun shouldReturnZeroForExploreTrip() {
            // when
            val result = stampQueryService.getNextStampOrderByTrip(exploreTrip)

            // then
            assertThat(result).isEqualTo(0)
        }

        @Test
        @DisplayName("코스형 여행이라면 다음 스탬프 순서를 반환한다.")
        fun shouldReturnNextStampOrderForCourseTrip() {
            // given
            given(stampQueryRepository.findNextStampOrderByTripId(courseTrip.id)).willReturn(3)

            // when
            val result = stampQueryService.getNextStampOrderByTrip(courseTrip)

            // then
            assertThat(result).isEqualTo(3)
        }
    }

    @Nested
    @DisplayName("getStampsToShiftAfterDeleted 메서드는")
    inner class GetStampsToShiftAfterDeleted {
        @Test
        @DisplayName("시프트할 스탬프가 존재하지 않으면 빈 리스트를 반환한다.")
        fun shouldReturnEmptyListWhenStampsToShiftDoNotExist() {
            // given
            val tripId = courseTrip.id
            val deletedOrder = courseStamp2.stampOrder
            courseStamp2.updateDeletedAt()
            given(stampQueryRepository.findStampsToShiftAfterOrder(tripId, deletedOrder)).willReturn(emptyList())

            // when
            val result = stampQueryService.getStampsToShiftAfterDeleted(tripId, deletedOrder)

            // then
            assertThat(result).isEqualTo(emptyList<Stamp>())
        }

        @Test
        @DisplayName("시프트할 스탬프 목록을 조회하고 반환한다.")
        fun shouldReturnStampsToShiftAfterDeleted() {
            // given
            val tripId = courseTrip.id
            val deletedOrder = courseStamp1.stampOrder
            given(stampQueryRepository.findStampsToShiftAfterOrder(tripId, deletedOrder)).willReturn(listOf(courseStamp2))

            // when
            val result = stampQueryService.getStampsToShiftAfterDeleted(tripId, deletedOrder)

            // then
            assertThat(result).hasSize(1)
            assertThat(result).containsExactly(courseStamp2)
        }
    }
}
