package com.ject.studytrip.studylog.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.studylog.domain.error.StudyLogErrorCode
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.repository.StudyLogQueryRepository
import com.ject.studytrip.studylog.domain.repository.StudyLogRepository
import com.ject.studytrip.studylog.fixture.StudyLogFixture
import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.domain.model.TripReport
import com.ject.studytrip.trip.fixture.DailyGoalFixture
import com.ject.studytrip.trip.fixture.TripFixture
import com.ject.studytrip.trip.fixture.TripReportFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.SliceImpl
import java.util.Optional

@DisplayName("StudyLogQueryService 단위 테스트")
class StudyLogQueryServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var studyLogQueryService: StudyLogQueryService

    @Mock
    private lateinit var studyLogRepository: StudyLogRepository

    @Mock
    private lateinit var studyLogQueryRepository: StudyLogQueryRepository

    private lateinit var member: Member
    private lateinit var courseTrip: Trip
    private lateinit var dailyGoal: DailyGoal
    private lateinit var studyLog1: StudyLog
    private lateinit var studyLog2: StudyLog
    private lateinit var tripReport: TripReport

    private val pageable: Pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE)

    companion object {
        private const val DEFAULT_PAGE = 0
        private const val DEFAULT_SIZE = 5
    }

    @BeforeEach
    fun setUp() {
        member = MemberFixture.createMemberFromKakaoWithId(1L)
        courseTrip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE)
        dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, courseTrip)
        studyLog1 = StudyLogFixture(member, dailyGoal).createWithId(1L)
        studyLog2 = StudyLogFixture(member, dailyGoal).createWithId(2L)
        tripReport = TripReportFixture.createTripReportWithId(1L, member)
    }

    @Nested
    @DisplayName("getActiveStudyLogCountByMemberId 메서드는")
    inner class GetActiveStudyLogCountByMemberId {
        @Test
        @DisplayName("특정 멤버에 대한 학습 기록이 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenStudyLogForMemberDoesNotExist() {
            // given
            val memberId = member.id
            given(studyLogQueryRepository.countActiveStudyLogsByMemberId(memberId)).willReturn(0L)

            // when
            val result = studyLogQueryService.getActiveStudyLogCountByMemberId(memberId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 멤버에 대한 학습 기록이 존재하면 그 개수를 반환한다.")
        fun shouldReturnCountWhenStudyLogForMemberExists() {
            // given
            val memberId = member.id
            given(studyLogQueryRepository.countActiveStudyLogsByMemberId(memberId)).willReturn(2L)

            // when
            val result = studyLogQueryService.getActiveStudyLogCountByMemberId(memberId)

            // then
            assertThat(result).isEqualTo(2L)
        }
    }

    @Nested
    @DisplayName("getStudyLogsSliceByTripId 메서드는")
    inner class GetStudyLogsSliceByTripId {
        @Test
        @DisplayName("특정 여행에 대한 학습 로그 목록을 페이징 처리와 최신순으로 정렬하여 반환한다.")
        fun shouldReturnStudyLogsByTripIdPagedAndSortedByLatest() {
            // given
            val tripId = courseTrip.id
            val order = "LATEST"
            val studyLogs = listOf(studyLog1, studyLog2)
            val mockSlice = SliceImpl(studyLogs, pageable, false)
            given(studyLogQueryRepository.findSliceByTripId(tripId, pageable, order)).willReturn(mockSlice)

            // when
            val result = studyLogQueryService.getStudyLogsSliceByTripId(tripId, DEFAULT_PAGE, DEFAULT_SIZE, order)

            // then
            assertThat(result.content).hasSize(studyLogs.size)
            assertThat(result).containsExactly(studyLog1, studyLog2)
        }

        @Test
        @DisplayName("특정 여행에 대한 학습 로그 목록을 페이징 처리와 과거순으로 정렬하여 반환한다.")
        fun shouldReturnStudyLogsByTripIdPagedAndSortedByOldest() {
            // given
            val tripId = courseTrip.id
            val order = "OLDEST"
            val studyLogs = listOf(studyLog2, studyLog1) // 과거순이므로 순서 반대
            val mockSlice = SliceImpl(studyLogs, pageable, false)
            given(studyLogQueryRepository.findSliceByTripId(tripId, pageable, order)).willReturn(mockSlice)

            // when
            val result = studyLogQueryService.getStudyLogsSliceByTripId(tripId, DEFAULT_PAGE, DEFAULT_SIZE, order)

            // then
            assertThat(result.content).hasSize(studyLogs.size)
            assertThat(result).containsExactly(studyLog2, studyLog1)
        }
    }

    @Nested
    @DisplayName("getValidStudyLog 메서드는")
    inner class GetValidStudyLog {
        @Test
        @DisplayName("학습 로그가 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenStudyLogDoesNotExist() {
            // given
            val studyLogId = -1L
            given(studyLogRepository.findById(studyLogId)).willReturn(Optional.empty())

            // when
            val exception = assertThrows<CustomException> { studyLogQueryService.getValidStudyLog(studyLogId) }

            // then
            assertThat(exception.message).isEqualTo(StudyLogErrorCode.STUDY_LOG_NOT_FOUND.message)
        }

        @Test
        @DisplayName("학습 로그가 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenStudyLogAlreadyDeleted() {
            // given
            val studyLogId = studyLog1.id
            studyLog1.updateDeletedAt()
            given(studyLogRepository.findById(studyLogId)).willReturn(Optional.of(studyLog1))

            // when
            val exception = assertThrows<CustomException> { studyLogQueryService.getValidStudyLog(studyLogId) }

            // then
            assertThat(exception.message).isEqualTo(StudyLogErrorCode.STUDY_LOG_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("학습 로그가 존재하면 학습 로그를 반환한다.")
        fun shouldReturnStudyLogWhenStudyLogExists() {
            // given
            val studyLogId = studyLog1.id
            given(studyLogRepository.findById(studyLogId)).willReturn(Optional.of(studyLog1))

            // when
            val result = studyLogQueryService.getValidStudyLog(studyLogId)

            // then
            assertThat(result).isEqualTo(studyLog1)
        }
    }

    @Nested
    @DisplayName("getStudyLogCountByTripId 메서드는")
    inner class GetStudyLogCountByTripId {
        @Test
        @DisplayName("특정 여행에 대한 학습 로그가 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenStudyLogForTripDoesNotExist() {
            // given
            val tripId = -1L
            given(studyLogQueryRepository.countStudyLogsByTripId(tripId)).willReturn(0L)

            // when
            val result = studyLogQueryService.getStudyLogCountByTripId(tripId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 여행에 대한 데일리 목표가 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDailyGoalForTripDoesNotExist() {
            // given
            val tripId = courseTrip.id
            dailyGoal.updateDeletedAt()
            given(studyLogQueryRepository.countStudyLogsByTripId(tripId)).willReturn(0L)

            // when
            val result = studyLogQueryService.getStudyLogCountByTripId(tripId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 여행에 대한 학습 로그가 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenStudyLogForTripExists() {
            // given
            val tripId = courseTrip.id
            given(studyLogQueryRepository.countStudyLogsByTripId(tripId)).willReturn(2L)

            // when
            val result = studyLogQueryService.getStudyLogCountByTripId(tripId)

            // then
            assertThat(result).isEqualTo(2L)
        }
    }

    @Nested
    @DisplayName("getStudyLogsSliceByTripReportId 메서드는")
    inner class GetStudyLogsSliceByTripReportId {
        @Test
        @DisplayName("특정 여행 리포트에 대한 학습 로그 목록을 페이징 처리와 최신순으로 정렬하여 반환한다.")
        fun shouldReturnStudyLogsByTripReportIdPagedAndSortedByLatest() {
            // given
            val tripReportId = tripReport.id
            val studyLogs = listOf(studyLog1, studyLog2)
            val mockSlice = SliceImpl(studyLogs, pageable, false)
            given(studyLogQueryRepository.findSliceByTripReportIdOrderByCreatedAtDesc(tripReportId, pageable)).willReturn(mockSlice)

            // when
            val result = studyLogQueryService.getStudyLogsSliceByTripReportId(tripReportId, DEFAULT_PAGE, DEFAULT_SIZE)

            // then
            assertThat(result.content).hasSize(studyLogs.size)
            assertThat(result).containsExactly(studyLog1, studyLog2)
        }
    }

    @Nested
    @DisplayName("getStudyLogIdsByTripId 메서드는")
    inner class GetStudyLogIdsByTripId {
        @Test
        @DisplayName("학습 로그가 존재하지 않으면 빈 리스트를 반환한다.")
        fun shouldReturnEmptyListWhenStudyLogDoesNotExist() {
            // given
            val tripId = -1L
            given(studyLogQueryRepository.findAllIdsByTripIdOrderByCreatedDesc(tripId)).willReturn(emptyList())

            // when
            val result = studyLogQueryService.getStudyLogIdsByTripId(tripId)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("학습 로그가 하나라도 존재하면 학습 로그 ID 목록을 반환한다.")
        fun shouldReturnStudyLogIdsWhenStudyLogExists() {
            // given
            val tripId = courseTrip.id
            val studyLogId1 = studyLog1.id
            val studyLogId2 = studyLog2.id
            val studyLogIds = listOf(studyLogId1, studyLogId2)
            given(studyLogQueryRepository.findAllIdsByTripIdOrderByCreatedDesc(tripId)).willReturn(studyLogIds)

            // when
            val result = studyLogQueryService.getStudyLogIdsByTripId(tripId)

            // then
            assertThat(result).hasSize(studyLogIds.size)
            assertThat(result).containsExactly(studyLogId1, studyLogId2)
        }
    }

    @Nested
    @DisplayName("getStudyLogImageUrlsByMemberId 메서드는")
    inner class GetStudyLogImageUrlsByMemberId {
        @Test
        @DisplayName("이미지가 존재하지 않으면 빈 리스트를 반환한다.")
        fun shouldReturnEmptyListWhenImagesDoNotExist() {
            // given
            val memberId = member.id
            given(studyLogQueryRepository.findImageUrlsByMemberId(memberId)).willReturn(emptyList())

            // when
            val result = studyLogQueryService.getStudyLogImageUrlsByMemberId(memberId)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("이미지가 존재하면 학습 로그 이미지 URL 목록을 반환한다.")
        fun shouldReturnStudyLogImageUrlsWhenImagesExist() {
            // given
            val memberId = member.id
            val imageUrls = listOf("https://cdn.example.com/studylogs/1.jpg", "https://cdn.example.com/studylogs/2.jpg")
            given(studyLogQueryRepository.findImageUrlsByMemberId(memberId)).willReturn(imageUrls)

            // when
            val result = studyLogQueryService.getStudyLogImageUrlsByMemberId(memberId)

            // then
            assertThat(result).hasSize(imageUrls.size)
            assertThat(result).isEqualTo(imageUrls)
        }
    }
}
