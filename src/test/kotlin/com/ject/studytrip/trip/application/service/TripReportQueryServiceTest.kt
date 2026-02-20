package com.ject.studytrip.trip.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.trip.domain.error.TripReportErrorCode
import com.ject.studytrip.trip.domain.model.TripReport
import com.ject.studytrip.trip.domain.repository.TripReportQueryRepository
import com.ject.studytrip.trip.domain.repository.TripReportRepository
import com.ject.studytrip.trip.fixture.TripReportFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.given
import java.util.Optional

@DisplayName("TripReportQueryService 단위 테스트")
class TripReportQueryServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var tripReportQueryService: TripReportQueryService

    @Mock
    private lateinit var tripReportRepository: TripReportRepository

    @Mock
    private lateinit var tripReportQueryRepository: TripReportQueryRepository

    private lateinit var member: Member
    private lateinit var tripReport1: TripReport
    private lateinit var tripReport2: TripReport
    private lateinit var tripReports: List<TripReport>

    @BeforeEach
    fun setUp() {
        member = MemberFixture().createFromKakaoWithId(1L)
        tripReport1 = TripReportFixture(member).createWithId(1L)
        tripReport2 = TripReportFixture(member).createWithId(2L)
        tripReports = listOf(tripReport1, tripReport2)
    }

    @Nested
    @DisplayName("getTripReport 메서드는")
    inner class GetTripReport {
        @Test
        @DisplayName("여행 리포트가 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenTripReportDoesNotExist() {
            // given
            val tripReportId = -1L
            given(tripReportRepository.findById(tripReportId)).willReturn(Optional.empty())

            // when
            val exception = assertThrows<CustomException> { tripReportQueryService.getTripReport(tripReportId) }

            // then
            assertThat(exception.message).isEqualTo(TripReportErrorCode.TRIP_REPORT_NOT_FOUND.message)
        }

        @Test
        @DisplayName("여행 리포트가 존재하면 여행을 반환한다.")
        fun shouldReturnTripWhenTripReportExists() {
            // given
            val tripReportId = tripReport1.id.requireId()
            given(tripReportRepository.findById(tripReportId)).willReturn(Optional.of(tripReport1))

            // when
            val result = tripReportQueryService.getTripReport(tripReportId)

            // then
            assertThat(result).isEqualTo(tripReport1)
        }
    }

    @Nested
    @DisplayName("getValidTripReport 메서드는")
    inner class GetValidTripReport {
        @Test
        @DisplayName("여행 리포트가 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenTripReportDoesNotExist() {
            // given
            val tripReportId = -1L
            given(tripReportRepository.findById(tripReportId)).willReturn(Optional.empty())

            // when
            val exception = assertThrows<CustomException> { tripReportQueryService.getValidTripReport(member.id.requireId(), tripReportId) }

            // then
            assertThat(exception.message).isEqualTo(TripReportErrorCode.TRIP_REPORT_NOT_FOUND.message)
        }

        @Test
        @DisplayName("멤버가 여행 리포트의 소유자가 아니라면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMemberIsNotTripReportOwner() {
            // given
            val memberId = -1L
            val tripReportId = tripReport1.id.requireId()
            given(tripReportRepository.findById(tripReportId)).willReturn(Optional.of(tripReport1))

            // when
            val exception = assertThrows<CustomException> { tripReportQueryService.getValidTripReport(memberId, tripReportId) }

            // then
            assertThat(exception.message).isEqualTo(TripReportErrorCode.NOT_TRIP_REPORT_OWNER.message)
        }

        @Test
        @DisplayName("여행 리포트가 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenTripReportAlreadyDeleted() {
            // given
            val tripReportId = tripReport1.id.requireId()
            tripReport1.updateDeletedAt()
            given(tripReportRepository.findById(tripReportId)).willReturn(Optional.of(tripReport1))

            // when
            val exception = assertThrows<CustomException> { tripReportQueryService.getValidTripReport(member.id.requireId(), tripReportId) }

            // then
            assertThat(exception.message).isEqualTo(TripReportErrorCode.TRIP_REPORT_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("여행 리포트가 존재하면 여행을 반환한다.")
        fun shouldReturnTripWhenTripReportExists() {
            // given
            val tripReportId = tripReport1.id.requireId()
            given(tripReportRepository.findById(tripReportId)).willReturn(Optional.of(tripReport1))

            // when
            val result = tripReportQueryService.getValidTripReport(member.id.requireId(), tripReportId)

            // then
            assertThat(result).isEqualTo(tripReport1)
        }
    }

    @Nested
    @DisplayName("getTripReportsByMemberId 메서드는")
    inner class GetTripReportsByMemberId {
        @Test
        @DisplayName("여행 리포트가 존재하지 않으면 빈 리스트를 반환한다.")
        fun shouldReturnEmptyListWhenTripReportDoesNotExist() {
            // given
            val memberId = member.id.requireId()
            given(tripReportQueryRepository.findAllActiveByMemberId(memberId)).willReturn(emptyList())

            // when
            val result = tripReportQueryService.getTripReportsByMemberId(memberId)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("여행 리포트가 하나라도 존재하면 특정 멤버가 생성한 여행 리포트 목록을 반환한다.")
        fun shouldReturnTripReports() {
            // given
            val memberId = member.id.requireId()
            given(tripReportQueryRepository.findAllActiveByMemberId(memberId)).willReturn(tripReports)

            // when
            val result = tripReportQueryService.getTripReportsByMemberId(memberId)

            // then
            assertThat(result).hasSize(2)
            assertThat(result).containsExactly(tripReport1, tripReport2)
        }
    }

    @Nested
    @DisplayName("getTripReportImageUrlsByMemberId 메서드는")
    inner class GetTripReportImageUrlsByMemberId {
        @Test
        @DisplayName("이미지가 존재하지 않으면 빈 리스트를 반환한다.")
        fun shouldReturnEmptyListWhenImagesDoNotExist() {
            // given
            val memberId = member.id.requireId()
            given(tripReportQueryRepository.findImageUrlsByMemberId(memberId)).willReturn(emptyList())

            // when
            val result = tripReportQueryService.getTripReportImageUrlsByMemberId(memberId)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("이미지가 존재하면 여행 리포트 이미지 URL 목록을 반환한다.")
        fun shouldReturnTripReportImageUrlsWhenImagesExist() {
            // given
            val memberId = member.id.requireId()
            val imageUrls = listOf("https://cdn.example.com/reports/1.jpg", "https://cdn.example.com/reports/2.jpg")
            given(tripReportQueryRepository.findImageUrlsByMemberId(memberId)).willReturn(imageUrls)

            // when
            val result = tripReportQueryService.getTripReportImageUrlsByMemberId(memberId)

            // then
            assertThat(result).hasSize(2)
            assertThat(result).isEqualTo(imageUrls)
        }
    }
}
