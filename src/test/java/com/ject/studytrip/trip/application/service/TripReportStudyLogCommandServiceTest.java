package com.ject.studytrip.trip.application.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.fixture.StudyLogFixture;
import com.ject.studytrip.trip.domain.model.*;
import com.ject.studytrip.trip.domain.repository.TripReportStudyLogRepository;
import com.ject.studytrip.trip.fixture.DailyGoalFixture;
import com.ject.studytrip.trip.fixture.TripFixture;
import com.ject.studytrip.trip.fixture.TripReportFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("TripReportStudyLogCommandService 단위 테스트")
class TripReportStudyLogCommandServiceTest extends BaseUnitTest {
    @InjectMocks private TripReportStudyLogCommandService tripReportStudyLogCommandService;
    @Mock private TripReportStudyLogRepository tripReportStudyLogRepository;

    private TripReport tripReport;
    private List<StudyLog> studyLogs;

    @BeforeEach
    void setUp() {
        Member member = MemberFixture.createMemberFromKakaoWithId(1L);
        Trip trip = TripFixture.createTrip(member, TripCategory.COURSE);
        DailyGoal dailyGoal = DailyGoalFixture.createDailyGoal(trip);
        StudyLog studyLog1 = StudyLogFixture.createStudyLogWithId(1L, member, dailyGoal);
        StudyLog studyLog2 = StudyLogFixture.createStudyLogWithId(2L, member, dailyGoal);
        tripReport = TripReportFixture.createTripReportWithId(1L, member);
        studyLogs = List.of(studyLog1, studyLog2);
    }

    @Nested
    @DisplayName("createTripReportStudyLogs 메서드는")
    class CreateTripReportStudyLogs {

        @Test
        @DisplayName("여행 리포트와 학습 로그 목록으로 여행 리포트 학습 로그를 생성한다.")
        void shouldCreateTripReportStudyLogs() {
            // given
            willDoNothing().given(tripReportStudyLogRepository).saveAll(anyList());

            // when
            tripReportStudyLogCommandService.createTripReportStudyLogs(tripReport, studyLogs);

            // then
            verify(tripReportStudyLogRepository, times(1)).saveAll(anyList());
        }
    }
}
