package com.ject.studytrip.studylog.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("StudyLogDailyMissionCommandService 단위 테스트")
class StudyLogDailyMissionCommandServiceTest extends BaseUnitTest {
    @InjectMocks private StudyLogDailyMissionCommandService studyLogDailyMissionCommandService;
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
                    studyLogDailyMissionCommandService.createStudyLogDailyMissions(
                            studyLog, dailyMissions);

            // then
            assertThat(result.size()).isEqualTo(dailyMissions.size());
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
            long result = studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissions();

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
            long result = studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissions();

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
                    studyLogDailyMissionCommandService
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
                    studyLogDailyMissionCommandService
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
                    studyLogDailyMissionCommandService
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
                    studyLogDailyMissionCommandService
                            .hardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteStudyLogDailyMissionsByMember 메서드는")
    class HardDeleteStudyLogDailyMissionsByMember {

        @Test
        @DisplayName("특정 멤버가 소유한 학습 로그 데일리 미션이 없으면 0을 반환한다.")
        void shouldReturnZeroWhenStudyLogDailyMissionsOwnedByMemberDoNotExist() {
            // given
            Long memberId = 1L;
            given(studyLogDailyMissionQueryRepository.deleteAllByMemberId(memberId)).willReturn(0L);

            // when
            long result =
                    studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissionsByMember(
                            memberId);

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("특정 멤버가 소유한 학습 로그 데일리 미션이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenStudyLogDailyMissionsOwnedByMemberExist() {
            // given
            Long memberId = 1L;
            given(studyLogDailyMissionQueryRepository.deleteAllByMemberId(memberId)).willReturn(5L);

            // when
            long result =
                    studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissionsByMember(
                            memberId);

            // then
            assertThat(result).isEqualTo(5L);
        }
    }
}
