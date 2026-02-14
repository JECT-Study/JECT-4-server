package com.ject.studytrip.trip.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.fixture.StudyLogFixture
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.domain.model.TripReport
import com.ject.studytrip.trip.domain.repository.TripReportStudyLogCommandRepository
import com.ject.studytrip.trip.domain.repository.TripReportStudyLogRepository
import com.ject.studytrip.trip.fixture.DailyGoalFixture
import com.ject.studytrip.trip.fixture.TripFixture
import com.ject.studytrip.trip.fixture.TripReportFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify

@DisplayName("TripReportStudyLogCommandService 단위 테스트")
class TripReportStudyLogCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var tripReportStudyLogCommandService: TripReportStudyLogCommandService

    @Mock
    private lateinit var tripReportStudyLogRepository: TripReportStudyLogRepository

    @Mock
    private lateinit var tripReportStudyLogCommandRepository: TripReportStudyLogCommandRepository

    private lateinit var member: Member
    private lateinit var tripReport: TripReport
    private lateinit var studyLogs: List<StudyLog>

    @BeforeEach
    fun setUp() {
        member = MemberFixture().createFromKakaoWithId(1L)
        val trip = TripFixture(member, TripCategory.COURSE).create()
        val dailyGoal = DailyGoalFixture(trip).create()
        val studyLog1 = StudyLogFixture(member, dailyGoal).createWithId(1L)
        val studyLog2 = StudyLogFixture(member, dailyGoal).createWithId(2L)
        tripReport = TripReportFixture(member).createWithId(1L)
        studyLogs = listOf(studyLog1, studyLog2)
    }

    @Nested
    @DisplayName("createTripReportStudyLogs 메서드는")
    inner class CreateTripReportStudyLogs {
        @Test
        @DisplayName("여행 리포트 학습 로그를 생성한다.")
        fun shouldCreateTripReportStudyLogs() {
            // when
            tripReportStudyLogCommandService.createTripReportStudyLogs(tripReport, studyLogs)

            // then
            verify(tripReportStudyLogRepository).saveAll(anyList())
        }
    }

    @Nested
    @DisplayName("hardDeleteTripReportStudyLogsOwnedByDeletedMember 메서드는")
    inner class HardDeleteTripReportStudyLogsOwnedByDeletedMember {
        @Test
        @DisplayName("삭제된 멤버가 소유한 여행 리포트 학습 로그가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenTripReportStudyLogsOwnedByDeletedMemberDoNotExist() {
            // given
            given(tripReportStudyLogCommandRepository.deleteAllByDeletedMemberOwner()).willReturn(0L)

            // when
            val result = tripReportStudyLogCommandService.hardDeleteTripReportStudyLogsOwnedByDeletedMember()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 멤버가 소유한 여행 리포트 학습 로그가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenTripReportStudyLogsOwnedByDeletedMemberExist() {
            // given
            given(tripReportStudyLogCommandRepository.deleteAllByDeletedMemberOwner()).willReturn(5L)

            // when
            val result = tripReportStudyLogCommandService.hardDeleteTripReportStudyLogsOwnedByDeletedMember()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteTripReportStudyLogsOwnedByMember 메서드는")
    inner class HardDeleteTripReportStudyLogsOwnedByMember {
        @Test
        @DisplayName("특정 멤버가 소유한 여행 리포트 학습 로그가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenTripReportStudyLogsOwnedByMemberDoNotExist() {
            // given
            val memberId = member.id
            given(tripReportStudyLogCommandRepository.deleteAllByMemberId(memberId)).willReturn(0L)

            // when
            val result = tripReportStudyLogCommandService.hardDeleteTripReportStudyLogsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 멤버가 소유한 여행 리포트 학습 로그가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenTripReportStudyLogsOwnedByMemberExist() {
            // given
            val memberId = member.id
            given(tripReportStudyLogCommandRepository.deleteAllByMemberId(memberId)).willReturn(5L)

            // when
            val result = tripReportStudyLogCommandService.hardDeleteTripReportStudyLogsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(5L)
        }
    }
}
