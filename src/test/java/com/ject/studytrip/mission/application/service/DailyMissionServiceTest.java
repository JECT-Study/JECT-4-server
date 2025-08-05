package com.ject.studytrip.mission.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.mission.domain.error.DailyMissionErrorCode;
import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.policy.DailyMissionPolicy;
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
        mission = MissionFixture.createMissionWithId(1L, stamp, 1);
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
    @DisplayName("validateSelectedMissions 메서드는")
    class validateSelectedMissions {

        @Test
        @DisplayName("코스형 여행에서 선택한 DailyMission들이 모두 동일한 스탬프를 가질 경우 예외가 발생하지 않는다")
        void shouldNotThrowExceptionWhenAllStampsAreSameInCourseTrip() {
            // given
            Stamp stamp = StampFixture.createStampWithId(1L, courseTrip, 1);
            Mission mission1 = MissionFixture.createMissionWithId(1L, stamp, 1);
            Mission mission2 = MissionFixture.createMissionWithId(2L, stamp, 2);

            DailyGoal dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, courseTrip);

            DailyMission dailyMission1 =
                    DailyMissionFixture.createDailyMissionWithId(1L, mission1, dailyGoal);
            DailyMission dailyMission2 =
                    DailyMissionFixture.createDailyMissionWithId(2L, mission2, dailyGoal);

            List<DailyMission> dailyMissions = List.of(dailyMission1, dailyMission2);

            // when & then
            assertDoesNotThrow(
                    () ->
                            DailyMissionPolicy.validateCourseTripStampConsistency(
                                    TripCategory.COURSE, dailyMissions));
        }

        @Test
        @DisplayName("코스형 여행에서 선택한 DailyMission 중 하나라도 다른 스탬프를 가지면 예외가 발생한다")
        void shouldThrowExceptionWhenStampsAreDifferentInCourseTrip() {
            // given
            Stamp stamp1 = StampFixture.createStampWithId(1L, courseTrip, 1);
            Stamp stamp2 = StampFixture.createStampWithId(2L, courseTrip, 2);
            Mission mission1 = MissionFixture.createMissionWithId(1L, stamp1, 1);
            Mission mission2 = MissionFixture.createMissionWithId(2L, stamp2, 1);

            DailyGoal dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, courseTrip);

            DailyMission dailyMission1 =
                    DailyMissionFixture.createDailyMissionWithId(1L, mission1, dailyGoal);
            DailyMission dailyMission2 =
                    DailyMissionFixture.createDailyMissionWithId(2L, mission2, dailyGoal);

            List<DailyMission> dailyMissions = List.of(dailyMission1, dailyMission2);

            // when & then
            assertThatThrownBy(
                            () ->
                                    DailyMissionPolicy.validateCourseTripStampConsistency(
                                            TripCategory.COURSE, dailyMissions))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DailyMissionErrorCode.COURSE_TRIP_STAMP_MISMATCH.getMessage());
        }
    }
}
