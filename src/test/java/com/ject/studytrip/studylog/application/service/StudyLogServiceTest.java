package com.ject.studytrip.studylog.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.studylog.domain.error.StudyLogErrorCode;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.repository.StudyLogQueryRepository;
import com.ject.studytrip.studylog.domain.repository.StudyLogRepository;
import com.ject.studytrip.studylog.fixture.StudyLogFixture;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.DailyGoalFixture;
import com.ject.studytrip.trip.fixture.TripFixture;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("StudyLogService 단위 테스트")
class StudyLogServiceTest extends BaseUnitTest {

    @InjectMocks private StudyLogService studyLogService;
    @Mock private StudyLogRepository studyLogRepository;
    @Mock private StudyLogQueryRepository studyLogQueryRepository;

    private Member member;
    private Trip courseTrip;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMemberFromKakaoWithId(1L);
        courseTrip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
    }

    @Nested
    @DisplayName("getActiveStudyLogCountByMemberId 메서드는")
    class GetActiveStudyLogCountByMemberId {

        @Test
        @DisplayName("해당 멤버의 학습 기록이 존재하지 않으면 0을 반환한다.")
        void shouldReturnZeroWhenStudyLogDoesNotExistForMember() {
            // given
            given(studyLogQueryRepository.countActiveStudyLogsByMemberId(member.getId()))
                    .willReturn(0L);

            // when
            long result = studyLogService.getActiveStudyLogCountByMemberId(member.getId());

            // then
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("해당 멤버의 학습 기록이 존재하면 그 개수를 반환한다.")
        void shouldReturnCountWhenStudyLogExistsForMember() {
            // given
            given(studyLogQueryRepository.countActiveStudyLogsByMemberId(member.getId()))
                    .willReturn(3L);

            // when
            long result = studyLogService.getActiveStudyLogCountByMemberId(member.getId());

            // then
            assertThat(result).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("createStudyLog 메서드는")
    class createStudyLog {

        @Test
        @DisplayName("학습 로그를 생성해 저장하고 반환한다")
        void shouldReturnCreateStudyLog() {
            // given
            DailyGoal dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, courseTrip);
            String content = "TEST content";

            given(studyLogRepository.save(any()))
                    .willAnswer(
                            invocation -> {
                                StudyLog studyLog = invocation.getArgument(0);
                                ReflectionTestUtils.setField(studyLog, "id", 1L);
                                return studyLog;
                            });

            // when
            StudyLog result = studyLogService.createStudyLog(member, dailyGoal, content);

            // then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getMember()).isEqualTo(member);
            assertThat(result.getDailyGoal()).isEqualTo(dailyGoal);
            assertThat(result.getTitle()).isEqualTo(dailyGoal.getTitle());
            assertThat(result.getContent()).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("getStudyLogsSliceByTripId 메서드는")
    class getStudyLogsSliceByTripId {

        @Test
        @DisplayName("특정 여행의 학습 로그 목록을 페이징 처리와 최신순으로 정렬하고 반환한다")
        void shouldReturnStudyLogsByTripIdWithSlice() {
            // given
            DailyGoal dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, courseTrip);

            StudyLog studyLog1 = StudyLogFixture.createStudyLogWithId(1L, member, dailyGoal);
            StudyLog studyLog2 = StudyLogFixture.createStudyLogWithId(2L, member, dailyGoal);
            List<StudyLog> studyLogs = List.of(studyLog1, studyLog2);

            int page = 0;
            int size = 5;
            Pageable pageable = PageRequest.of(page, size);

            Slice<StudyLog> mockSlice = new SliceImpl<>(studyLogs, pageable, false);

            given(
                            studyLogQueryRepository.findSliceByTripIdOrderByCreatedAtDesc(
                                    courseTrip.getId(), pageable))
                    .willReturn(mockSlice);

            // when
            Slice<StudyLog> result =
                    studyLogService.getStudyLogsSliceByTripId(courseTrip.getId(), page, size);

            // then
            assertThat(result.getContent().size()).isEqualTo(studyLogs.size());
            assertThat(result.getContent().get(0)).isEqualTo(studyLog1);
            assertThat(result.getContent().get(1)).isEqualTo(studyLog2);
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogs 메서드는")
    class HardDeleteStudyLogs {

        @Test
        @DisplayName("삭제된 학습 로그가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDeletedStudyLogsDoNotExist() {
            // given
            given(studyLogQueryRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L);

            // when
            long result = studyLogService.hardDeleteStudyLogs();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 학습 로그가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDeletedStudyLogsExist() {
            // given
            given(studyLogQueryRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L);

            // when
            long result = studyLogService.hardDeleteStudyLogs();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogsOwnedByDeletedMember 메서드는")
    class HardDeleteStudyLogsOwnedByDeletedMember {

        @Test
        @DisplayName("삭제된 멤버가 소유한 학습 로그가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenStudyLogsOwnedByDeletedMemberDoNotExist() {
            // given
            given(studyLogQueryRepository.deleteAllByDeletedMemberOwner()).willReturn(0L);

            // when
            long result = studyLogService.hardDeleteStudyLogsOwnedByDeletedMember();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 멤버가 소유한 학습 로그가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenStudyLogsOwnedByDeletedMemberExist() {
            // given
            given(studyLogQueryRepository.deleteAllByDeletedMemberOwner()).willReturn(5L);

            // when
            long result = studyLogService.hardDeleteStudyLogsOwnedByDeletedMember();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogsOwnedByDeletedDailyGoal 메서드는")
    class HardDeleteStudyLogsOwnedByDeletedDailyGoal {

        @Test
        @DisplayName("삭제된 데일리 목표가 소유한 학습 로그가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenStudyLogsOwnedByDeletedDailyGoalDoNotExist() {
            // given
            given(studyLogQueryRepository.deleteAllByDeletedDailyGoalOwner()).willReturn(0L);

            // when
            long result = studyLogService.hardDeleteStudyLogsOwnedByDeletedDailyGoal();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 데일리 목표가 소유한 학습 로그가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenStudyLogsOwnedByDeletedDailyGoalExist() {
            // given
            given(studyLogQueryRepository.deleteAllByDeletedDailyGoalOwner()).willReturn(5L);

            // when
            long result = studyLogService.hardDeleteStudyLogsOwnedByDeletedDailyGoal();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("getValidStudyLogById 메서드는")
    class GetValidStudyLogById {

        @Test
        @DisplayName("존재하지 않는 학습 로그 ID로 조회하면 예외가 발생한다")
        void shouldThrowExceptionWhenStudyLogNotFound() {
            // given
            Long invalidId = -1L;
            given(studyLogRepository.findById(invalidId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> studyLogService.getValidStudyLogById(invalidId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StudyLogErrorCode.STUDY_LOG_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("삭제된 학습 로그를 조회하면 예외가 발생한다")
        void shouldThrowExceptionWhenStudyLogIsDeleted() {
            // given
            DailyGoal dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, courseTrip);
            StudyLog studyLog = StudyLogFixture.createStudyLogWithId(1L, member, dailyGoal);
            studyLog.updateDeletedAt();

            given(studyLogRepository.findById(1L)).willReturn(Optional.of(studyLog));

            // when & then
            assertThatThrownBy(() -> studyLogService.getValidStudyLogById(1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StudyLogErrorCode.STUDY_LOG_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("유효한 학습 로그 ID로 조회하면 학습 로그를 반환한다")
        void shouldReturnStudyLogWhenIdIsValid() {
            // given
            DailyGoal dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, courseTrip);
            StudyLog studyLog = StudyLogFixture.createStudyLogWithId(1L, member, dailyGoal);

            given(studyLogRepository.findById(1L)).willReturn(Optional.of(studyLog));

            // when
            StudyLog result = studyLogService.getValidStudyLogById(1L);

            // then
            assertThat(result).isEqualTo(studyLog);
            assertThat(result.getDeletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("updateImageUrl 메서드는")
    class UpdateImageUrl {
        private static final String NEW_IMAGE_URL =
                "https://cdn.example.com/study-logs/1/image.jpg";

        @Test
        @DisplayName("삭제된 학습 로그의 이미지 URL을 수정하면 예외가 발생한다")
        void shouldThrowExceptionWhenStudyLogIsDeleted() {
            // given
            DailyGoal dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, courseTrip);
            StudyLog studyLog = StudyLogFixture.createStudyLogWithId(1L, member, dailyGoal);
            studyLog.updateDeletedAt();

            // when & then
            assertThatThrownBy(() -> studyLogService.updateImageUrl(studyLog, NEW_IMAGE_URL))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(StudyLogErrorCode.STUDY_LOG_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("유효한 학습 로그의 이미지 URL을 수정한다")
        void shouldUpdateImageUrlWhenStudyLogIsValid() {
            // given
            DailyGoal dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, courseTrip);
            StudyLog studyLog = StudyLogFixture.createStudyLogWithId(1L, member, dailyGoal);
            String oldImageUrl = studyLog.getImageUrl();

            // when
            studyLogService.updateImageUrl(studyLog, NEW_IMAGE_URL);

            // then
            assertThat(studyLog.getImageUrl()).isEqualTo(NEW_IMAGE_URL);
            assertThat(studyLog.getImageUrl()).isNotEqualTo(oldImageUrl);
        }
    }
}
