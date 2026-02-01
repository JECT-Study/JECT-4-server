package com.ject.studytrip.trip.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.studylog.fixture.StudyLogFixture
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.domain.model.TripReport
import com.ject.studytrip.trip.domain.repository.TripReportCommandRepository
import com.ject.studytrip.trip.domain.repository.TripReportRepository
import com.ject.studytrip.trip.fixture.CreateTripReportRequestFixture
import com.ject.studytrip.trip.fixture.DailyGoalFixture
import com.ject.studytrip.trip.fixture.TripFixture
import com.ject.studytrip.trip.fixture.TripReportFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any

@DisplayName("TripReportCommandService 단위 테스트")
class TripReportCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var tripReportCommandService: TripReportCommandService

    @Mock
    private lateinit var tripReportRepository: TripReportRepository

    @Mock
    private lateinit var tripReportCommandRepository: TripReportCommandRepository

    private lateinit var member: Member
    private lateinit var studyLogIds: List<Long>
    private lateinit var tripReport: TripReport

    @BeforeEach
    fun setUp() {
        member = MemberFixture.createMemberFromKakaoWithId(1L)
        val trip = TripFixture(member, TripCategory.COURSE).create()
        val dailyGoal = DailyGoalFixture(trip).create()
        val studyLog1 = StudyLogFixture(member, dailyGoal).createWithId(1L)
        val studyLog2 = StudyLogFixture(member, dailyGoal).createWithId(2L)
        studyLogIds = listOf(studyLog1.id, studyLog2.id)
        tripReport = TripReportFixture(member).create()
    }

    @Nested
    @DisplayName("createTripReport 메서드는")
    inner class CreateTripReport {
        private val fixture = CreateTripReportRequestFixture()

        @Test
        @DisplayName("요청이 유효하면 여행 리포트를 생성하고 반환한다.")
        fun shouldCreateAndReturnTripReportWhenRequestIsValid() {
            // given
            val request = fixture.withStudyLogIds(studyLogIds).build()
            given(tripReportRepository.save(any())).willReturn(tripReport)

            // when
            val result = tripReportCommandService.createTripReport(member, request)

            // then
            assertThat(result).isEqualTo(tripReport)
        }
    }

    @Nested
    @DisplayName("updateImageUrl 메서드는")
    inner class UpdateImageUrl {
        private val newImageUrl = "https://cdn.example.com/trip-reports/1/image.jpg"

        @Test
        @DisplayName("유효한 여행 리포트의 이미지 URL을 수정한다.")
        fun shouldUpdateTripReportImageUrlWhenTripReportIsValid() {
            // given
            val oldImageUrl = tripReport.imageUrl

            // when
            tripReportCommandService.updateImageUrl(tripReport, newImageUrl)

            // then
            assertThat(tripReport.imageUrl).isEqualTo(newImageUrl)
            assertThat(tripReport.imageUrl).isNotEqualTo(oldImageUrl)
        }
    }

    @Nested
    @DisplayName("deleteTripReport 메서드는")
    inner class DeleteTripReport {
        @Test
        @DisplayName("여행 리포트가 삭제될 때 deletedAt 필드를 현재 시간으로 업데이트한다. (소프트 삭제)")
        fun shouldUpdateDeletedAtWhenTripReportIsDeleted() {
            // when
            tripReportCommandService.deleteTripReport(tripReport)

            // then
            assertThat(tripReport.deletedAt).isNotNull
        }
    }

    @Nested
    @DisplayName("hardDeleteTripReports 메서드는")
    inner class HardDeleteTripReports {
        @Test
        @DisplayName("삭제된 여행 리포트가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDeletedTripReportsDoNotExist() {
            // given
            given(tripReportCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L)

            // when
            val result = tripReportCommandService.hardDeleteTripReports()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 여행 리포트가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDeletedTripReportsExist() {
            // given
            given(tripReportCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L)

            // when
            val result = tripReportCommandService.hardDeleteTripReports()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteTripReportsOwnedByDeletedMember 메서드는")
    inner class HardDeleteTripReportsOwnedByDeletedMember {
        @Test
        @DisplayName("삭제된 멤버가 소유한 여행 리포트가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenTripReportsOwnedByDeletedMemberDoNotExist() {
            // given
            given(tripReportCommandRepository.deleteAllByDeletedMemberOwner()).willReturn(0L)

            // when
            val result = tripReportCommandService.hardDeleteTripReportsOwnedByDeletedMember()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 멤버가 소유한 여행 리포트가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenTripReportsOwnedByDeletedMemberExist() {
            // given
            given(tripReportCommandRepository.deleteAllByDeletedMemberOwner()).willReturn(5L)

            // when
            val result = tripReportCommandService.hardDeleteTripReportsOwnedByDeletedMember()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteTripReportsOwnedByMember 메서드는")
    inner class HardDeleteTripReportsOwnedByMember {
        @Test
        @DisplayName("특정 멤버가 소유한 여행 리포트가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenTripReportsOwnedByMemberDoNotExist() {
            // given
            val memberId = -1L
            given(tripReportCommandRepository.deleteAllByMemberId(memberId)).willReturn(0L)

            // when
            val result = tripReportCommandService.hardDeleteTripReportsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 멤버가 소유한 여행 리포트가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenTripReportsOwnedByMemberExist() {
            // given
            val memberId = member.id
            given(tripReportCommandRepository.deleteAllByMemberId(memberId)).willReturn(5L)

            // when
            val result = tripReportCommandService.hardDeleteTripReportsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(5L)
        }
    }
}
