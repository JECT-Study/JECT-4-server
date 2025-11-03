package com.ject.studytrip.mission.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.repository.DailyMissionQueryRepository;
import com.ject.studytrip.mission.domain.repository.DailyMissionRepository;
import com.ject.studytrip.mission.fixture.DailyMissionFixture;
import com.ject.studytrip.mission.fixture.MissionFixture;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.fixture.StampFixture;
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

@DisplayName("DailyMissionCommandService 단위 테스트")
class DailyMissionCommandServiceTest extends BaseUnitTest {
    @InjectMocks private DailyMissionCommandService dailyMissionCommandService;
    @Mock private DailyMissionRepository dailyMissionRepository;
    @Mock private DailyMissionQueryRepository dailyMissionQueryRepository;

    private Mission mission;
    private DailyGoal dailyGoal;
    private DailyMission dailyMission;

    @BeforeEach
    void setUp() {
        Member member = MemberFixture.createMemberFromKakaoWithId(1L);
        Trip courseTrip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
        Stamp stamp = StampFixture.createStampWithId(1L, courseTrip, 1);
        mission = MissionFixture.createMissionWithId(1L, stamp);
        dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, courseTrip);
        dailyMission = DailyMissionFixture.createDailyMissionWithId(1L, mission, dailyGoal);
    }

    @Nested
    @DisplayName("createDailyMissions 메서드는")
    class CreateDailyMissions {

        @Test
        @DisplayName("미션 리스트로 데일리 미션을 생성하여 저장하고 반환한다")
        void shouldCreateDailyMissions() {
            // given
            List<Mission> missions = List.of(mission);
            given(dailyMissionRepository.saveAll(any())).willReturn(List.of(dailyMission));

            // when
            List<DailyMission> result =
                    dailyMissionCommandService.createDailyMissions(dailyGoal, missions);

            // then
            assertThat(result.size()).isEqualTo(1);
            assertThat(result.get(0).getDailyGoal().getId()).isEqualTo(dailyGoal.getId());
        }
    }

    @Nested
    @DisplayName("deleteDailyMission 메서드는")
    class DeleteDailyMission {

        @Test
        @DisplayName("삭제 시 deletedAt을 현재 시각으로 설정한다")
        void shouldDeleteDailyMission() {
            // when
            dailyMissionCommandService.deleteDailyMission(dailyMission);

            // then
            assertThat(dailyMission.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("hardDeleteDailyMissions 메서드는")
    class HardDeleteDailyMissions {

        @Test
        @DisplayName("삭제된 데일리 미션이 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDeletedDailyMissionsDoNotExist() {
            // given
            given(dailyMissionQueryRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L);

            // when
            long result = dailyMissionCommandService.hardDeleteDailyMissions();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 데일리 미션이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDeletedDailyMissionsExist() {
            // given
            given(dailyMissionQueryRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L);

            // when
            long result = dailyMissionCommandService.hardDeleteDailyMissions();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteDailyMissionsOwnedByDeletedMission 메서드는")
    class HardDeleteDailyMissionsOwnedByDeletedMission {

        @Test
        @DisplayName("삭제된 미션이 소유한 데일리 미션이 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDailyMissionsOwnedByDeletedMissionDoNotExist() {
            // given
            given(dailyMissionQueryRepository.deleteAllByDeletedMissionOwner()).willReturn(0L);

            // when
            long result = dailyMissionCommandService.hardDeleteDailyMissionsOwnedByDeletedMission();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 미션이 소유한 데일리 미션이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDailyMissionsOwnedByDeletedMissionExist() {
            // given
            given(dailyMissionQueryRepository.deleteAllByDeletedMissionOwner()).willReturn(5L);

            // when
            long result = dailyMissionCommandService.hardDeleteDailyMissionsOwnedByDeletedMission();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteDailyMissionsOwnedByDeletedDailyGoal 메서드는")
    class HardDeleteDailyMissionsOwnedByDeletedDailyGoal {

        @Test
        @DisplayName("삭제된 데일리 목표가 소유한 데일리 미션이 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDailyMissionsOwnedByDeletedDailyGoalDoNotExist() {
            // given
            given(dailyMissionQueryRepository.deleteAllByDeletedDailyGoalOwner()).willReturn(0L);

            // when
            long result =
                    dailyMissionCommandService.hardDeleteDailyMissionsOwnedByDeletedDailyGoal();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 데일리 목표가 소유한 데일리 미션이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDailyMissionsOwnedByDeletedDailyGoalExist() {
            // given
            given(dailyMissionQueryRepository.deleteAllByDeletedDailyGoalOwner()).willReturn(5L);

            // when
            long result =
                    dailyMissionCommandService.hardDeleteDailyMissionsOwnedByDeletedDailyGoal();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteDailyMissionsByMember 메서드는")
    class HardDeleteDailyMissionsByMember {

        @Test
        @DisplayName("특정 멤버가 소유한 데일리 미션이 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDailyMissionsOwnedByMemberDoNotExist() {
            // given
            Long memberId = 1L;
            given(dailyMissionQueryRepository.deleteAllByMemberId(memberId)).willReturn(0L);

            // when
            long result = dailyMissionCommandService.hardDeleteDailyMissionsByMember(memberId);

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("특정 멤버가 소유한 데일리 미션이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDailyMissionsOwnedByMemberExist() {
            // given
            Long memberId = 1L;
            given(dailyMissionQueryRepository.deleteAllByMemberId(memberId)).willReturn(5L);

            // when
            long result = dailyMissionCommandService.hardDeleteDailyMissionsByMember(memberId);

            // then
            assertThat(result).isEqualTo(5L);
        }
    }
}
