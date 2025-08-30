package com.ject.studytrip.studylog.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.fixture.DailyMissionFixture;
import com.ject.studytrip.mission.fixture.MissionFixture;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.fixture.StampFixture;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionQueryRepository;
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionRepository;
import com.ject.studytrip.studylog.fixture.StudyLogFixture;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.DailyGoalFixture;
import com.ject.studytrip.trip.fixture.TripFixture;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("StudyLogDailyMissionService 단위 테스트")
public class StudyLogDailyMissionServiceTest extends BaseUnitTest {
    @InjectMocks private StudyLogDailyMissionService studyLogDailyMissionService;
    @Mock private StudyLogDailyMissionRepository studyLogDailyMissionRepository;
    @Mock private StudyLogDailyMissionQueryRepository studyLogDailyMissionQueryRepository;

    private Member member;
    private Mission mission1;
    private Mission mission2;
    private DailyGoal dailyGoal;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMemberFromKakaoWithId(1L);
        Trip trip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
        Stamp stamp = StampFixture.createStampWithId(1L, trip, 1);
        mission1 = MissionFixture.createMissionWithId(1L, stamp);
        mission2 = MissionFixture.createMissionWithId(2L, stamp);
        dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, trip);
    }

    @Nested
    @DisplayName("createStudyLogDailyMissions 메서드는")
    class createStudyLogDailyMissions {

        @Test
        @DisplayName("학습 로그와 데일리 미션 목록으로 학습 로그 데일리 미션을 생성하여 저장하고 반환한다")
        void shouldReturnCreateStudyLogDailyMissions() {
            // given
            StudyLog studyLog = StudyLogFixture.createStudyLogWithId(1L, member, dailyGoal);

            DailyMission dailyMission1 =
                    DailyMissionFixture.createDailyMissionWithId(1L, mission1, dailyGoal);
            DailyMission dailyMission2 =
                    DailyMissionFixture.createDailyMissionWithId(2L, mission2, dailyGoal);
            List<DailyMission> dailyMissions = List.of(dailyMission1, dailyMission2);

            given(studyLogDailyMissionRepository.saveAll(anyList()))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            List<StudyLogDailyMission> result =
                    studyLogDailyMissionService.createStudyLogDailyMissions(
                            studyLog, dailyMissions);

            // then
            assertThat(result.size()).isEqualTo(dailyMissions.size());
        }
    }

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
                    studyLogDailyMissionService.getGroupedStudyLogDailyMissionsByStudyLogIds(
                            studyLogIds);

            // then
            assertThat(result).isEqualTo(mockResult);
            verify(studyLogDailyMissionQueryRepository, times(1))
                    .findStudyLogDailyMissionsGroupedByStudyLogId(studyLogIds);
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogDailyMissions 메서드는")
    class HardDeleteStudyLogDailyMissions {

        @Test
        @DisplayName("삭제된 StudyLogDailyMission이 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDeletedStudyLogDailyMissionsDoNotExist() {
            // given
            given(studyLogDailyMissionQueryRepository.deleteAllByDeletedAtIsNotNull())
                    .willReturn(0L);

            // when
            long result = studyLogDailyMissionService.hardDeleteStudyLogDailyMissions();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 StudyLogDailyMission이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDeletedStudyLogDailyMissionsExist() {
            // given
            given(studyLogDailyMissionQueryRepository.deleteAllByDeletedAtIsNotNull())
                    .willReturn(5L);

            // when
            long result = studyLogDailyMissionService.hardDeleteStudyLogDailyMissions();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogDailyMissionsOwnedByDeletedDailyMission 메서드는")
    class HardDeleteStudyLogDailyMissionsOwnedByDeletedDailyMission {

        @Test
        @DisplayName("삭제된 데일리 미션이 소유한 StudyLogDailyMission이 없으면 0을 반환한다.")
        void shouldReturnZeroWhenStudyLogDailyMissionsOwnedByDeletedDailyMissionDoNotExist() {
            // given
            given(studyLogDailyMissionQueryRepository.deleteAllByDeletedDailyMissionOwner())
                    .willReturn(0L);

            // when
            long result =
                    studyLogDailyMissionService
                            .hardDeleteStudyLogDailyMissionsOwnedByDeletedDailyMission();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 데일리 미션이 소유한 StudyLogDailyMission이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenStudyLogDailyMissionsOwnedByDeletedDailyMissionExist() {
            // given
            given(studyLogDailyMissionQueryRepository.deleteAllByDeletedDailyMissionOwner())
                    .willReturn(5L);

            // when
            long result =
                    studyLogDailyMissionService
                            .hardDeleteStudyLogDailyMissionsOwnedByDeletedDailyMission();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog 메서드는")
    class HardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog {

        @Test
        @DisplayName("삭제된 학습 로그가 소유한 StudyLogDailyMission이 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDailyMissionsOwnedByDeletedStudyLogDoNotExist() {
            // given
            given(studyLogDailyMissionQueryRepository.deleteAllByDeletedStudyLogOwner())
                    .willReturn(0L);

            // when
            long result =
                    studyLogDailyMissionService
                            .hardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 학습 로그가 소유한 StudyLogDailyMission이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenStudyLogDailyMissionsOwnedByDeletedStudyLogExist() {
            // given
            given(studyLogDailyMissionQueryRepository.deleteAllByDeletedStudyLogOwner())
                    .willReturn(5L);

            // when
            long result =
                    studyLogDailyMissionService
                            .hardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }
}
