package com.ject.studytrip.studylog.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionQueryRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("StudyLogDailyMissionQueryService 단위 테스트")
class StudyLogDailyMissionQueryServiceTest extends BaseUnitTest {
    @InjectMocks private StudyLogDailyMissionQueryService studyLogDailyMissionQueryService;
    @Mock private StudyLogDailyMissionQueryRepository studyLogDailyMissionQueryRepository;

    @Nested
    @DisplayName("getGroupedStudyLogDailyMissionsByStudyLogIds 메서드는")
    class getGroupedStudyLogDailyMissionsByStudyLogIds {

        @Test
        @DisplayName("학습 로그 ID 리스트로 그룹화된 StudyLogDailyMission Map을 반환한다")
        void shouldReturnGroupedStudyLogDailyMissionMap() {
            // given
            Long studyLogId1 = 1L;
            Long studyLogId2 = 2L;

            StudyLogDailyMission studyLogDailyMission1 = mock(StudyLogDailyMission.class);
            StudyLogDailyMission studyLogDailyMission2 = mock(StudyLogDailyMission.class);
            StudyLogDailyMission studyLogDailyMission3 = mock(StudyLogDailyMission.class);

            Map<Long, List<StudyLogDailyMission>> mockResult = new HashMap<>();
            mockResult.put(studyLogId1, List.of(studyLogDailyMission1, studyLogDailyMission2));
            mockResult.put(studyLogId2, List.of(studyLogDailyMission3));

            List<Long> studyLogIds = List.of(studyLogId1, studyLogId2);

            given(
                            studyLogDailyMissionQueryRepository
                                    .findStudyLogDailyMissionsGroupedByStudyLogId(studyLogIds))
                    .willReturn(mockResult);

            // when
            Map<Long, List<StudyLogDailyMission>> result =
                    studyLogDailyMissionQueryService.getGroupedStudyLogDailyMissionsByStudyLogIds(
                            studyLogIds);

            // then
            assertThat(result).isEqualTo(mockResult);
            verify(studyLogDailyMissionQueryRepository, times(1))
                    .findStudyLogDailyMissionsGroupedByStudyLogId(studyLogIds);
        }
    }
}
