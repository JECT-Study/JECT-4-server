package com.ject.studytrip.mission.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.mission.domain.error.DailyMissionErrorCode;
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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DailyMissionService 단위 테스트")
public class DailyMissionServiceTest extends BaseUnitTest {

    @InjectMocks private DailyMissionService dailyMissionService;
    @Mock private DailyMissionRepository dailyMissionRepository;
    @Mock private DailyMissionQueryRepository dailyMissionQueryRepository;

    private Trip courseTrip;
    private DailyGoal dailyGoal;
    private Mission mission;
    private DailyMission dailyMission;

    @BeforeEach
    void setUp() {
        Member member = MemberFixture.createMemberFromKakaoWithId(1L);
        courseTrip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
        Stamp stamp = StampFixture.createStampWithId(1L, courseTrip, 1);
        mission = MissionFixture.createMissionWithId(1L, stamp);
        dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, courseTrip);
        dailyMission = DailyMissionFixture.createDailyMissionWithId(1L, mission, dailyGoal);
    }

    @Nested
    @DisplayName("데일리 미션을 생성한다")
    class CreateDailyMission {

        @Test
        @DisplayName("미션 리스트로 데일리 미션을 생성하여 저장하고 반환한다")
        void shouldCreateDailyMissions() {
            // given
            List<Mission> missions = List.of(mission);
            given(dailyMissionRepository.saveAll(any())).willReturn(List.of(dailyMission));

            // when
            List<DailyMission> result =
                    dailyMissionService.createDailyMissions(dailyGoal, missions);

            // then
            assertThat(result.size()).isEqualTo(1);
            assertThat(result.get(0).getDailyGoal().getId()).isEqualTo(dailyGoal.getId());
        }
    }

    @Nested
    @DisplayName("데일리 미션을 삭제한다")
    class DeleteDailyMission {

        @Test
        @DisplayName("삭제 시 deletedAt을 현재 시각으로 설정한다")
        void shouldDeleteDailyMission() {
            // when
            dailyMissionService.deleteDailyMission(dailyMission);

            // then
            assertThat(dailyMission.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("데일리 미션 목록 조회")
    class ListDailyMissions {

        @Test
        @DisplayName("ID 리스트로 유효한 데일리 미션을 조회해 반환한다")
        void shouldGetDailyMissionsByIds() {
            // given
            List<Long> ids = List.of(dailyMission.getId());
            List<DailyMission> dailyMissions = List.of(dailyMission);
            given(dailyMissionRepository.findAllByIdIn(ids)).willReturn(dailyMissions);

            // when
            List<DailyMission> result =
                    dailyMissionService.getValidDailyMissionsByIds(dailyGoal.getId(), ids);

            // then
            assertThat(result.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("요청한 ID 개수와 조회된 데일리 미션 개수가 다르면 예외가 발생한다")
        void shouldThrowExceptionWhenSomeDailyMissionsDoNotExist() {
            // given
            List<Long> ids = List.of(1L, 2L);
            given(dailyMissionRepository.findAllByIdIn(ids)).willReturn(List.of(dailyMission));

            // when & then
            assertThatThrownBy(
                            () ->
                                    dailyMissionService.getValidDailyMissionsByIds(
                                            dailyGoal.getId(), ids))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DailyMissionErrorCode.DAILY_MISSION_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("데일리 미션이 요청한 데일리 목표에 속하지 않으면 예외를 던진다")
        void shouldThrowExceptionWhenDailyMissionDoesNotBelongToDailyGoal() {
            // given
            DailyGoal otherGoal = DailyGoalFixture.createDailyGoalWithId(999L, dailyGoal.getTrip());
            List<Long> ids = List.of(dailyMission.getId());
            given(dailyMissionRepository.findAllByIdIn(ids)).willReturn(List.of(dailyMission));

            // when & then
            assertThatThrownBy(
                            () ->
                                    dailyMissionService.getValidDailyMissionsByIds(
                                            otherGoal.getId(), ids))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(
                            DailyMissionErrorCode.DAILY_MISSION_NOT_BELONG_TO_DAILY_GOAL
                                    .getMessage());
        }

        @Test
        @DisplayName("데일리 미션이 이미 삭제된 경우 예외가 발생한다")
        void shouldThrowExceptionWhenDailyMissionIsDeleted() {
            // given
            dailyMission.updateDeletedAt(); // deleted
            given(dailyMissionRepository.findAllByIdIn(List.of(1L)))
                    .willReturn(List.of(dailyMission));

            // when & then
            Assertions.assertThatThrownBy(
                            () ->
                                    dailyMissionService.getValidDailyMissionsByIds(
                                            dailyGoal.getId(), List.of(dailyMission.getId())))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DailyMissionErrorCode.DAILY_MISSION_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("데일리 목표 ID로 데일리 미션 목록을 반환한다")
        void shouldGetDailyMissionsByDailyGoalId() {
            // given
            Long goalId = dailyGoal.getId();
            List<DailyMission> missions = List.of(dailyMission);
            given(dailyMissionQueryRepository.findAllByDailyGoalIdFetchJoinMission(goalId))
                    .willReturn(missions);

            // when
            List<DailyMission> result = dailyMissionService.getDailyMissionsByDailyGoal(goalId);

            // then
            assertThat(result.isEmpty()).isFalse();
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
            long result = dailyMissionService.hardDeleteDailyMissions();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 데일리 미션이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDeletedDailyMissionsExist() {
            // given
            given(dailyMissionQueryRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L);

            // when
            long result = dailyMissionService.hardDeleteDailyMissions();

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
            long result = dailyMissionService.hardDeleteDailyMissionsOwnedByDeletedMission();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 미션이 소유한 데일리 미션이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDailyMissionsOwnedByDeletedMissionExist() {
            // given
            given(dailyMissionQueryRepository.deleteAllByDeletedMissionOwner()).willReturn(5L);

            // when
            long result = dailyMissionService.hardDeleteDailyMissionsOwnedByDeletedMission();

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
            long result = dailyMissionService.hardDeleteDailyMissionsOwnedByDeletedDailyGoal();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 데일리 목표가 소유한 데일리 미션이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDailyMissionsOwnedByDeletedDailyGoalExist() {
            // given
            given(dailyMissionQueryRepository.deleteAllByDeletedDailyGoalOwner()).willReturn(5L);

            // when
            long result = dailyMissionService.hardDeleteDailyMissionsOwnedByDeletedDailyGoal();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }
}
