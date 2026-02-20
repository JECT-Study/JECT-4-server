package com.ject.studytrip.studylog.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.studylog.domain.error.StudyLogErrorCode
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.repository.StudyLogCommandRepository
import com.ject.studytrip.studylog.domain.repository.StudyLogRepository
import com.ject.studytrip.studylog.fixture.StudyLogFixture
import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.fixture.DailyGoalFixture
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
import org.mockito.kotlin.any
import org.springframework.test.util.ReflectionTestUtils

@DisplayName("StudyLogCommandService 단위 테스트")
class StudyLogCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var studyLogCommandService: StudyLogCommandService

    @Mock
    private lateinit var studyLogRepository: StudyLogRepository

    @Mock
    private lateinit var studyLogCommandRepository: StudyLogCommandRepository

    private lateinit var member: Member
    private lateinit var courseTrip: Trip
    private lateinit var dailyGoal: DailyGoal
    private lateinit var studyLog: StudyLog

    @BeforeEach
    fun setUp() {
        member = MemberFixture().createFromKakaoWithId(1L)
        courseTrip = TripFixture(member, TripCategory.COURSE).createWithId(1L)
        dailyGoal = DailyGoalFixture(courseTrip).createWithId(1L)
        studyLog = StudyLogFixture(member, dailyGoal).createWithId(1L)
    }

    @Nested
    @DisplayName("createStudyLog 메서드는")
    inner class CreateStudyLog {
        @Test
        @DisplayName("학습 로그를 생성하고 반환한다.")
        fun shouldCreateAndReturnStudyLog() {
            // given
            val content = "TEST CONTENT"
            given(studyLogRepository.save(any()))
                .willAnswer { invocation ->
                    val studyLog = invocation.getArgument<StudyLog>(0)
                    ReflectionTestUtils.setField(studyLog, "id", 1L)
                    studyLog
                }

            // when
            val result = studyLogCommandService.createStudyLog(member, dailyGoal, content)

            // then
            assertThat(result.id).isEqualTo(1L)
            assertThat(result.title).isEqualTo(dailyGoal.title)
            assertThat(result.content).isEqualTo(content)
        }
    }

    @Nested
    @DisplayName("updateImageUrl 메서드는")
    inner class UpdateImageUrl {
        private val newImageUrl = "https://cdn.example.com/study-logs/1/image.jpg"

        @Test
        @DisplayName("삭제된 학습 로그의 이미지 URL을 수정하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenStudyLogIsDeleted() {
            // given
            studyLog.updateDeletedAt()

            // when
            val exception = assertThrows<CustomException> { studyLogCommandService.updateImageUrl(studyLog, newImageUrl) }

            // then
            assertThat(exception.message).isEqualTo(StudyLogErrorCode.STUDY_LOG_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("유효한 학습 로그의 이미지 URL을 수정한다.")
        fun shouldUpdateStudyLogImageUrlWhenStudyLogIsValid() {
            // given
            val oldImageUrl = studyLog.imageUrl

            // when
            studyLogCommandService.updateImageUrl(studyLog, newImageUrl)

            // then
            assertThat(studyLog.imageUrl).isEqualTo(newImageUrl)
            assertThat(studyLog.imageUrl).isNotEqualTo(oldImageUrl)
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogs 메서드는")
    inner class HardDeleteStudyLogs {
        @Test
        @DisplayName("삭제된 학습 로그가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDeletedStudyLogsDoNotExist() {
            // given
            given(studyLogCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L)

            // when
            val result = studyLogCommandService.hardDeleteStudyLogs()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 학습 로그가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDeletedStudyLogsExist() {
            // given
            given(studyLogCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L)

            // when
            val result = studyLogCommandService.hardDeleteStudyLogs()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogsOwnedByDeletedMember 메서드는")
    inner class HardDeleteStudyLogsOwnedByDeletedMember {
        @Test
        @DisplayName("삭제된 멤버가 소유한 학습 로그가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenStudyLogsOwnedByDeletedMemberDoNotExist() {
            // given
            given(studyLogCommandRepository.deleteAllByDeletedMemberOwner()).willReturn(0L)

            // when
            val result = studyLogCommandService.hardDeleteStudyLogsOwnedByDeletedMember()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 멤버가 소유한 학습 로그가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenStudyLogsOwnedByDeletedMemberExist() {
            // given
            given(studyLogCommandRepository.deleteAllByDeletedMemberOwner()).willReturn(5L)

            // when
            val result = studyLogCommandService.hardDeleteStudyLogsOwnedByDeletedMember()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogsOwnedByDeletedDailyGoal 메서드는")
    inner class HardDeleteStudyLogsOwnedByDeletedDailyGoal {
        @Test
        @DisplayName("삭제된 데일리 목표가 소유한 학습 로그가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenStudyLogsOwnedByDeletedDailyGoalDoNotExist() {
            // given
            given(studyLogCommandRepository.deleteAllByDeletedDailyGoalOwner()).willReturn(0L)

            // when
            val result = studyLogCommandService.hardDeleteStudyLogsOwnedByDeletedDailyGoal()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 데일리 목표가 소유한 학습 로그가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenStudyLogsOwnedByDeletedDailyGoalExist() {
            // given
            given(studyLogCommandRepository.deleteAllByDeletedDailyGoalOwner()).willReturn(5L)

            // when
            val result = studyLogCommandService.hardDeleteStudyLogsOwnedByDeletedDailyGoal()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogsOwnedByMember 메서드는")
    inner class HardDeleteStudyLogsOwnedByMember {
        @Test
        @DisplayName("특정 멤버가 소유한 학습 로그가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenStudyLogsOwnedByMemberDoNotExist() {
            // given
            val memberId = member.id.requireId()
            given(studyLogCommandRepository.deleteByMemberId(memberId)).willReturn(0L)

            // when
            val result = studyLogCommandService.hardDeleteStudyLogsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("특정 멤버가 소유한 학습 로그가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenStudyLogsOwnedByMemberExist() {
            // given
            val memberId = member.id.requireId()
            given(studyLogCommandRepository.deleteByMemberId(memberId)).willReturn(5L)

            // when
            val result = studyLogCommandService.hardDeleteStudyLogsOwnedByMember(memberId)

            // then
            assertThat(result).isEqualTo(5L)
        }
    }
}
