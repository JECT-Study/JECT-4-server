package com.ject.studytrip.trip.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.trip.domain.error.TripErrorCode
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.domain.repository.TripQueryRepository
import com.ject.studytrip.trip.domain.repository.TripRepository
import com.ject.studytrip.trip.fixture.TripFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.given
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.SliceImpl
import java.util.Optional

@DisplayName("TripQueryService 단위 테스트")
class TripQueryServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var tripQueryService: TripQueryService

    @Mock
    private lateinit var tripRepository: TripRepository

    @Mock
    private lateinit var tripQueryRepository: TripQueryRepository

    private lateinit var member: Member
    private lateinit var trip: Trip

    private val pageable: Pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE)

    companion object {
        private const val DEFAULT_PAGE = 0
        private const val DEFAULT_SIZE = 5
    }

    @BeforeEach
    fun setUp() {
        member = MemberFixture().createFromKakaoWithId(1L)
        trip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
    }

    @Nested
    @DisplayName("getValidTrip 메서드는")
    inner class GetValidTrip {
        @Test
        @DisplayName("여행이 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenTripDoesNotExist() {
            // given
            val tripId = -1L
            given(tripRepository.findById(tripId)).willReturn(Optional.empty())

            // when
            val exception = assertThrows<CustomException> { tripQueryService.getValidTrip(member.id.requireId(), tripId) }

            // then
            assertThat(exception.message).isEqualTo(TripErrorCode.TRIP_NOT_FOUND.message)
        }

        @Test
        @DisplayName("멤버가 여행의 소유자가 아니라면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMemberIsNotTripOwner() {
            // given
            val memberId = -1L
            val tripId = trip.id.requireId()
            given(tripRepository.findById(tripId)).willReturn(Optional.of(trip))

            // when
            val exception = assertThrows<CustomException> { tripQueryService.getValidTrip(memberId, tripId) }

            // then
            assertThat(exception.message).isEqualTo(TripErrorCode.NOT_TRIP_OWNER.message)
        }

        @Test
        @DisplayName("여행이 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenTripAlreadyDeleted() {
            // given
            val tripId = trip.id.requireId()
            trip.updateDeletedAt()
            given(tripRepository.findById(tripId)).willReturn(Optional.of(trip))

            // when
            val exception = assertThrows<CustomException> { tripQueryService.getValidTrip(member.id.requireId(), tripId) }

            // then
            assertThat(exception.message).isEqualTo(TripErrorCode.TRIP_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("여행이 이미 완료되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenTripAlreadyCompleted() {
            // given
            val tripId = trip.id.requireId()
            trip.updateCompleted()
            given(tripRepository.findById(tripId)).willReturn(Optional.of(trip))

            // when
            val exception = assertThrows<CustomException> { tripQueryService.getValidTrip(member.id.requireId(), tripId) }

            // then
            assertThat(exception.message).isEqualTo(TripErrorCode.TRIP_ALREADY_COMPLETED.message)
        }

        @Test
        @DisplayName("여행이 존재하면 여행을 반환한다.")
        fun shouldReturnTripWhenTripExists() {
            // given
            val tripId = trip.id.requireId()
            given(tripRepository.findById(tripId)).willReturn(Optional.of(trip))

            // when
            val result = tripQueryService.getValidTrip(member.id.requireId(), tripId)

            // then
            assertThat(result).isEqualTo(trip)
        }
    }

    @Nested
    @DisplayName("getTripsSliceByMemberId 메서드는")
    inner class GetTripsSliceByMemberId {
        @Test
        @DisplayName("특정 멤버에 대한 여행 목록을 페이징 처리하여 반환한다.")
        fun shouldReturnTripsSliceByMemberIdPaged() {
            // given
            val memberId = member.id.requireId()
            val trips = listOf(trip)
            val mockSlice = SliceImpl(trips, pageable, false)
            given(tripQueryRepository.findSliceByMemberIdAndCompletedFalseAndDeletedAtIsNull(memberId, pageable)).willReturn(mockSlice)

            // when
            val result = tripQueryService.getTripsSliceByMemberId(memberId, DEFAULT_PAGE, DEFAULT_SIZE)

            // then
            assertThat(result.content).hasSize(trips.size)
            assertThat(result).containsExactly(trip)
        }
    }

    @Nested
    @DisplayName("getActiveTripCountByMemberId 메서드는")
    inner class GetActiveTripCountByMemberId {
        @Test
        @DisplayName("특정 멤버의 여행이 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenTripDoesNotExistForMember() {
            // given
            val memberId = member.id.requireId()
            given(tripQueryRepository.countActiveTripsByMemberIdAndCategory(memberId, TripCategory.COURSE)).willReturn(0L)
            given(tripQueryRepository.countActiveTripsByMemberIdAndCategory(memberId, TripCategory.EXPLORE)).willReturn(0L)

            // when
            val result = tripQueryService.getActiveTripCountByMemberId(memberId)

            // then
            assertThat(result.course).isEqualTo(0L)
            assertThat(result.explore).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 멤버의 코스형, 탐험형 여행 개수를 TripCount에 담아서 반환한다.")
        fun shouldReturnTripCountByMemberId() {
            // given
            val memberId = member.id.requireId()
            given(tripQueryRepository.countActiveTripsByMemberIdAndCategory(memberId, TripCategory.COURSE)).willReturn(3L)
            given(tripQueryRepository.countActiveTripsByMemberIdAndCategory(memberId, TripCategory.EXPLORE)).willReturn(2L)

            // when
            val result = tripQueryService.getActiveTripCountByMemberId(memberId)

            // then
            assertThat(result.course).isEqualTo(3L)
            assertThat(result.explore).isEqualTo(2L)
        }
    }

    @Nested
    @DisplayName("getValidCompletedTrip 메서드는")
    inner class GetValidCompletedTrip {
        @Test
        @DisplayName("여행이 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenTripDoesNotExist() {
            // given
            val tripId = -1L
            given(tripRepository.findById(tripId)).willReturn(Optional.empty())

            // when
            val exception = assertThrows<CustomException> { tripQueryService.getValidCompletedTrip(member.id.requireId(), tripId) }

            // then
            assertThat(exception.message).isEqualTo(TripErrorCode.TRIP_NOT_FOUND.message)
        }

        @Test
        @DisplayName("멤버가 여행의 소유자가 아니라면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMemberIsNotTripOwner() {
            // given
            val memberId = -1L
            val tripId = trip.id.requireId()
            given(tripRepository.findById(tripId)).willReturn(Optional.of(trip))

            // when
            val exception = assertThrows<CustomException> { tripQueryService.getValidCompletedTrip(memberId, tripId) }

            // then
            assertThat(exception.message).isEqualTo(TripErrorCode.NOT_TRIP_OWNER.message)
        }

        @Test
        @DisplayName("여행이 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenTripAlreadyDeleted() {
            // given
            val tripId = trip.id.requireId()
            trip.updateDeletedAt()
            given(tripRepository.findById(tripId)).willReturn(Optional.of(trip))

            // when
            val exception = assertThrows<CustomException> { tripQueryService.getValidCompletedTrip(member.id.requireId(), tripId) }

            // then
            assertThat(exception.message).isEqualTo(TripErrorCode.TRIP_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("여행이 아직 완료되지 않았다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenTripIsNotCompleted() {
            // given
            val tripId = trip.id.requireId()
            given(tripRepository.findById(tripId)).willReturn(Optional.of(trip))

            // when
            val exception = assertThrows<CustomException> { tripQueryService.getValidCompletedTrip(member.id.requireId(), tripId) }

            // then
            assertThat(exception.message).isEqualTo(TripErrorCode.TRIP_NOT_COMPLETED.message)
        }

        @Test
        @DisplayName("여행이 이미 완료되었다면 여행을 반환한다.")
        fun shouldReturnTripWhenTripAlreadyCompleted() {
            // given
            val tripId = trip.id.requireId()
            trip.updateCompleted()
            given(tripRepository.findById(tripId)).willReturn(Optional.of(trip))

            // when
            val result = tripQueryService.getValidCompletedTrip(member.id.requireId(), tripId)

            // then
            assertThat(result).isEqualTo(trip)
        }
    }
}
