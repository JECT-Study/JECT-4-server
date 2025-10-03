package com.ject.studytrip.studylog.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

@DisplayName("StudyLogQueryService 단위 테스트")
class StudyLogQueryServiceTest extends BaseUnitTest {
    @InjectMocks private StudyLogQueryService studyLogQueryService;
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
            Long memberId = member.getId();
            given(studyLogQueryRepository.countActiveStudyLogsByMemberId(memberId)).willReturn(0L);

            // when
            long result = studyLogQueryService.getActiveStudyLogCountByMemberId(memberId);

            // then
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("해당 멤버의 학습 기록이 존재하면 그 개수를 반환한다.")
        void shouldReturnCountWhenStudyLogExistsForMember() {
            // given
            Long memberId = member.getId();
            given(studyLogQueryRepository.countActiveStudyLogsByMemberId(memberId)).willReturn(3L);

            // when
            long result = studyLogQueryService.getActiveStudyLogCountByMemberId(memberId);

            // then
            assertThat(result).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("getStudyLogsSliceByTripId 메서드는")
    class getStudyLogsSliceByTripId {

        @Test
        @DisplayName("특정 여행의 학습 로그 목록을 페이징 처리와 최신순으로 정렬하고 반환한다")
        void shouldReturnStudyLogsByTripIdWithSlice() {
            // given
            Long tripId = courseTrip.getId();
            DailyGoal dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, courseTrip);
            StudyLog studyLog1 = StudyLogFixture.createStudyLogWithId(1L, member, dailyGoal);
            StudyLog studyLog2 = StudyLogFixture.createStudyLogWithId(2L, member, dailyGoal);
            List<StudyLog> studyLogs = List.of(studyLog1, studyLog2);

            int page = 0;
            int size = 5;
            Pageable pageable = PageRequest.of(page, size);

            Slice<StudyLog> mockSlice = new SliceImpl<>(studyLogs, pageable, false);

            given(studyLogQueryRepository.findSliceByTripIdOrderByCreatedAtDesc(tripId, pageable))
                    .willReturn(mockSlice);

            // when
            Slice<StudyLog> result =
                    studyLogQueryService.getStudyLogsSliceByTripId(tripId, page, size);

            // then
            assertThat(result.getContent().size()).isEqualTo(studyLogs.size());
            assertThat(result.getContent().get(0)).isEqualTo(studyLog1);
            assertThat(result.getContent().get(1)).isEqualTo(studyLog2);
        }
    }

    @Nested
    @DisplayName("getValidStudyLog 메서드는")
    class GetValidStudyLog {

        @Test
        @DisplayName("존재하지 않는 학습 로그 ID로 조회하면 예외가 발생한다")
        void shouldThrowExceptionWhenStudyLogNotFound() {
            // given
            Long invalidId = -1L;
            given(studyLogRepository.findById(invalidId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> studyLogQueryService.getValidStudyLog(invalidId))
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
            assertThatThrownBy(() -> studyLogQueryService.getValidStudyLog(1L))
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
            StudyLog result = studyLogQueryService.getValidStudyLog(1L);

            // then
            assertThat(result).isEqualTo(studyLog);
            assertThat(result.getDeletedAt()).isNull();
        }
    }
}
