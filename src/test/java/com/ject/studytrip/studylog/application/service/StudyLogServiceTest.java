package com.ject.studytrip.studylog.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
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
            String title = "TEST Title";
            String content = "TEST content";

            given(studyLogRepository.save(any()))
                    .willAnswer(
                            invocation -> {
                                StudyLog studyLog = invocation.getArgument(0);
                                ReflectionTestUtils.setField(studyLog, "id", 1L);
                                return studyLog;
                            });

            // when
            StudyLog result = studyLogService.createStudyLog(member, dailyGoal, title, content);

            // then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getMember()).isEqualTo(member);
            assertThat(result.getDailyGoal()).isEqualTo(dailyGoal);
            assertThat(result.getTitle()).isEqualTo(title);
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
}
