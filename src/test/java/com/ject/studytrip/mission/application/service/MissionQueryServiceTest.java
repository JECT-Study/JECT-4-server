package com.ject.studytrip.mission.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.mission.domain.error.MissionErrorCode;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.repository.MissionQueryRepository;
import com.ject.studytrip.mission.domain.repository.MissionRepository;
import com.ject.studytrip.mission.fixture.MissionFixture;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.fixture.StampFixture;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.TripFixture;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("MissionQueryService 단위 테스트")
class MissionQueryServiceTest extends BaseUnitTest {
    @InjectMocks private MissionQueryService missionQueryService;
    @Mock private MissionRepository missionRepository;
    @Mock private MissionQueryRepository missionQueryRepository;

    private Stamp courseStamp;
    private Stamp exploreStamp;
    private Mission courseMission;
    private Mission exploreMission1;
    private Mission exploreMission2;

    @BeforeEach
    void setUp() {
        Member member = MemberFixture.createMemberFromKakao();
        Trip courseTrip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
        Trip exploreTrip = TripFixture.createTripWithId(2L, member, TripCategory.EXPLORE);
        courseStamp = StampFixture.createStampWithId(1L, courseTrip, 1);
        exploreStamp = StampFixture.createStampWithId(2L, exploreTrip, 0);
        courseMission = MissionFixture.createMissionWithId(1L, courseStamp);
        exploreMission1 = MissionFixture.createMissionWithId(2L, exploreStamp);
        exploreMission2 = MissionFixture.createMissionWithId(3L, exploreStamp);
    }

    @Nested
    @DisplayName("getValidMission 메서드는")
    class GetValidMission {

        @Test
        @DisplayName("미션이 존재하지 않으면 예외가 발생한다.")
        void shouldThrowExceptionWhenMissionNotFound() {
            // given
            Long invalidMissionId = -1L;
            Long stampId = exploreStamp.getId();
            given(missionRepository.findById(invalidMissionId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> missionQueryService.getValidMission(stampId, invalidMissionId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("미션이 다른 스탬프에 속하면 예외가 발생한다.")
        void shouldThrowExceptionWhenMissionNotBelongToStamp() {
            // given
            Long missionId = exploreMission1.getId();
            Long invalidStampId = courseStamp.getId();
            given(missionRepository.findById(missionId)).willReturn(Optional.of(exploreMission1));

            // when & then
            assertThatThrownBy(() -> missionQueryService.getValidMission(invalidStampId, missionId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.getMessage());
        }

        @Test
        @DisplayName("미션이 이미 삭제된 경우 예외가 발생한다.")
        void shouldThrowExceptionWhenMissionIsDeleted() {
            // given
            Long missionId = exploreMission1.getId();
            Long stampId = exploreStamp.getId();
            exploreMission1.updateDeletedAt();
            given(missionRepository.findById(missionId)).willReturn(Optional.of(exploreMission1));

            // when & then
            assertThatThrownBy(() -> missionQueryService.getValidMission(stampId, missionId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("특정 스탬프에 속하고 삭제되지 않은 미션이 존재하면, 해당 미션을 반환한다.")
        void shouldReturnValidMission() {
            // given
            Long missionId = exploreMission1.getId();
            Long stampId = exploreStamp.getId();
            given(missionRepository.findById(missionId)).willReturn(Optional.of(exploreMission1));

            // when
            Mission result = missionQueryService.getValidMission(stampId, missionId);

            // then
            assertThat(result).isEqualTo(exploreMission1);
        }
    }

    @Nested
    @DisplayName("getMissionsByStampId 메서드는")
    class GetMissionsByStampId {

        @Test
        @DisplayName("특정 스탬프에 대한 삭제되지 않은 모든 미션을 생성일 순으로 반환한다.")
        void shouldReturnMissionsInOrderWhenStampIdExists() {
            // given
            Long stampId = exploreStamp.getId();
            given(missionRepository.findAllByStampIdAndDeletedAtIsNullOrderByCreatedAt(stampId))
                    .willReturn(List.of(exploreMission1, exploreMission2));

            // when
            List<Mission> result = missionQueryService.getMissionsByStampId(stampId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(exploreMission1, exploreMission2);
        }
    }

    @Nested
    @DisplayName("getValidMissionsWithStamp 메서드는")
    class GetValidMissionsWithStamp {

        @Test
        @DisplayName("요청한 ID 개수와 조회된 미션 개수가 다르면 예외가 발생한다")
        void shouldThrowExceptionWhenSomeMissionsDoNotExist() {
            // given
            List<Long> missionIds = List.of(1L, 2L);
            given(missionQueryRepository.findAllByIdsInFetchJoinStamp(missionIds))
                    .willReturn(List.of(courseMission));

            // when & then
            assertThatThrownBy(() -> missionQueryService.getValidMissionsWithStamp(missionIds))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("이미 삭제된 미션이 포함되어 있을 경우 예외가 발생한다")
        void shouldThrowExceptionWhenAnyMissionIsDeleted() {
            // given
            courseMission.updateDeletedAt(); // deleted
            given(missionQueryRepository.findAllByIdsInFetchJoinStamp(any()))
                    .willReturn(List.of(courseMission));

            // when & then
            assertThatThrownBy(() -> missionQueryService.getValidMissionsWithStamp(List.of(1L)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("이미 완료된 미션이 포함되어 있을 경우 예외가 발생한다")
        void shouldThrowExceptionWhenAnyMissionIsCompleted() {
            // given
            courseMission.updateCompleted();
            given(missionQueryRepository.findAllByIdsInFetchJoinStamp(any()))
                    .willReturn(List.of(courseMission));

            // when & then
            assertThatThrownBy(() -> missionQueryService.getValidMissionsWithStamp(List.of(1L)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_ALREADY_COMPLETED.getMessage());
        }

        @Test
        @DisplayName("유효한 미션 ID들로 요청 시 검증을 통과하고 미션 리스트를 반환한다")
        void shouldReturnValidMissions() {
            // given
            List<Long> missionIds = List.of(courseMission.getId());
            given(missionQueryRepository.findAllByIdsInFetchJoinStamp(missionIds))
                    .willReturn(List.of(courseMission));

            // when
            List<Mission> result = missionQueryService.getValidMissionsWithStamp(missionIds);

            // then
            assertThat(result).containsExactly(courseMission);
        }
    }
}
