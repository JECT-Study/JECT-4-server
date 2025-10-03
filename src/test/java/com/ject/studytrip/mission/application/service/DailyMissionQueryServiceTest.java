package com.ject.studytrip.mission.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

@DisplayName("DailyMissionQueryService 단위 테스트")
class DailyMissionQueryServiceTest extends BaseUnitTest {
    @InjectMocks private DailyMissionQueryService dailyMissionQueryService;
    @Mock private DailyMissionRepository dailyMissionRepository;
    @Mock private DailyMissionQueryRepository dailyMissionQueryRepository;

    private DailyGoal dailyGoal;
    private DailyMission dailyMission;

    @BeforeEach
    void setUp() {
        Member member = MemberFixture.createMemberFromKakaoWithId(1L);
        Trip courseTrip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
        Stamp stamp = StampFixture.createStampWithId(1L, courseTrip, 1);
        Mission mission = MissionFixture.createMissionWithId(1L, stamp);
        dailyGoal = DailyGoalFixture.createDailyGoalWithId(1L, courseTrip);
        dailyMission = DailyMissionFixture.createDailyMissionWithId(1L, mission, dailyGoal);
    }

    @Nested
    @DisplayName("getValidDailyMissionsByIds 메서드는")
    class GetValidDailyMissionsByIds {

        @Test
        @DisplayName("요청한 ID 개수와 조회된 데일리 미션 개수가 다르면 예외가 발생한다")
        void shouldThrowExceptionWhenSomeDailyMissionsDoNotExist() {
            // given
            List<Long> ids = List.of(1L, 2L);
            given(dailyMissionRepository.findAllByIdIn(ids)).willReturn(List.of(dailyMission));

            // when & then
            assertThatThrownBy(
                            () ->
                                    dailyMissionQueryService.getValidDailyMissionsByIds(
                                            dailyGoal.getId(), ids))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DailyMissionErrorCode.DAILY_MISSION_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("데일리 미션이 요청한 데일리 목표에 속하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenDailyMissionDoesNotBelongToDailyGoal() {
            // given
            DailyGoal otherGoal = DailyGoalFixture.createDailyGoalWithId(999L, dailyGoal.getTrip());
            List<Long> ids = List.of(dailyMission.getId());
            given(dailyMissionRepository.findAllByIdIn(ids)).willReturn(List.of(dailyMission));

            // when & then
            assertThatThrownBy(
                            () ->
                                    dailyMissionQueryService.getValidDailyMissionsByIds(
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
            List<Long> ids = List.of(dailyMission.getId());
            given(dailyMissionRepository.findAllByIdIn(ids)).willReturn(List.of(dailyMission));

            // when & then
            Assertions.assertThatThrownBy(
                            () ->
                                    dailyMissionQueryService.getValidDailyMissionsByIds(
                                            dailyGoal.getId(), ids))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DailyMissionErrorCode.DAILY_MISSION_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("ID 리스트로 유효한 데일리 미션을 조회해 반환한다")
        void shouldGetDailyMissionsByIds() {
            // given
            List<Long> ids = List.of(dailyMission.getId());
            List<DailyMission> dailyMissions = List.of(dailyMission);
            given(dailyMissionRepository.findAllByIdIn(ids)).willReturn(dailyMissions);

            // when
            List<DailyMission> result =
                    dailyMissionQueryService.getValidDailyMissionsByIds(dailyGoal.getId(), ids);

            // then
            assertThat(result.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("getDailyMissionsByDailyGoal 메서드는")
    class GetDailyMissionsByDailyGoal {

        @Test
        @DisplayName("데일리 목표 ID로 데일리 미션 목록을 반환한다")
        void shouldGetDailyMissionsByDailyGoalId() {
            // given
            Long dailyGoalId = dailyGoal.getId();
            List<DailyMission> missions = List.of(dailyMission);
            given(dailyMissionQueryRepository.findAllByDailyGoalIdFetchJoinMission(dailyGoalId))
                    .willReturn(missions);

            // when
            List<DailyMission> result =
                    dailyMissionQueryService.getDailyMissionsByDailyGoal(dailyGoalId);

            // then
            assertThat(result.isEmpty()).isFalse();
        }
    }
}
