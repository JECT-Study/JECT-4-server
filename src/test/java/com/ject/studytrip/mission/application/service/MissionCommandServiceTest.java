package com.ject.studytrip.mission.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
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
import com.ject.studytrip.mission.fixture.CreateMissionRequestFixture;
import com.ject.studytrip.mission.fixture.MissionFixture;
import com.ject.studytrip.mission.fixture.UpdateMissionRequestFixture;
import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest;
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.fixture.StampFixture;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.fixture.TripFixture;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("MissionCommandService 단위 테스트")
class MissionCommandServiceTest extends BaseUnitTest {
    private static final String NEW_MISSION_NAME = "NEW MISSION NAME";

    @InjectMocks private MissionCommandService missionCommandService;
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
    @DisplayName("createMission 메서드는")
    class CreateMission {
        private final CreateMissionRequestFixture fixture = new CreateMissionRequestFixture();

        @Test
        @DisplayName("코스형 여행 스탬프를 위한 미션을 생성하고 반환한다.")
        void shouldReturnMissionForCourseStamp() {
            // given
            CreateMissionRequest request = fixture.build();
            given(missionRepository.save(any(Mission.class))).willReturn(courseMission);

            // when
            Mission result = missionCommandService.createMission(courseStamp, request);

            // then
            assertThat(result).isEqualTo(courseMission);
            assertThat(result.getStamp()).isEqualTo(courseStamp);
        }

        @Test
        @DisplayName("탐험형 여행 스탬프를 위한 미션을 생성하고 반환한다.")
        void shouldReturnMissionForExploreStamp() {
            // given
            CreateMissionRequest request = fixture.build();
            given(missionRepository.save(any(Mission.class))).willReturn(exploreMission1);

            // when
            Mission result = missionCommandService.createMission(exploreStamp, request);

            // then
            assertThat(result).isEqualTo(exploreMission1);
            assertThat(result.getStamp()).isEqualTo(exploreStamp);
        }
    }

    @Nested
    @DisplayName("updateMissionNameAndMemoIfPresent 메서드는")
    class UpdateMissionNameAndMemoIfPresent {
        private final UpdateMissionRequestFixture fixture = new UpdateMissionRequestFixture();

        @Test
        @DisplayName("특정 미션의 이름을 수정하고 DB에 반영한다.")
        void shouldUpdateMissionName() {
            // given
            UpdateMissionRequest request = fixture.withName(NEW_MISSION_NAME).build();

            // when
            missionCommandService.updateMissionNameIfPresent(courseMission, request);

            // then
            assertThat(courseMission.getName()).isEqualTo(NEW_MISSION_NAME);
        }
    }

    @Nested
    @DisplayName("deleteMission 메서드는")
    class DeleteMission {

        @Test
        @DisplayName("미션을 삭제하면 deletedAt 필드에 현재 시각이 설정된다.")
        void shouldDeleteMission() {
            // given
            assertThat(courseMission.getDeletedAt()).isNull();
            LocalDateTime beforeDeletionTime = LocalDateTime.now();

            // when
            missionCommandService.deleteMission(courseMission);

            // then
            assertThat(courseMission.getDeletedAt()).isNotNull();
            assertThat(courseMission.getDeletedAt()).isAfterOrEqualTo(beforeDeletionTime);
        }
    }

    @Nested
    @DisplayName("completeMission 메서드는")
    class CompleteMission {

        @Test
        @DisplayName("삭제된 미션이면 예외가 발생한다")
        void shouldThrowExceptionWhenMissionIsDeleted() {
            // given
            exploreMission1.updateDeletedAt();

            // then
            assertThatThrownBy(() -> missionCommandService.completeMission(exploreMission1))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("이미 완료된 미션이면 예외가 발생한다")
        void shouldThrowExceptionWhenMissionIsAlreadyCompleted() {
            // given
            exploreMission1.updateCompleted();

            // then
            assertThatThrownBy(() -> missionCommandService.completeMission(exploreMission1))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_ALREADY_COMPLETED.getMessage());
        }

        @Test
        @DisplayName("정상적인 미션이면 completed 필드를 true로 업데이트한다")
        void shouldCompletedMission() {
            // when
            missionCommandService.completeMission(exploreMission1);

            // then
            assertThat(exploreMission1.isCompleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("validateMissionsBelongsToStamp 메서드는")
    class ValidateMissionsBelongsToStamp {

        @Test
        @DisplayName("미션 목록 중 다른 스탬프에 속한 미션이 존재하면 예외가 발생한다.")
        void shouldThrowExceptionWhenMissionNotBelongToStamp() {
            // given
            Long stampId = exploreStamp.getId();
            List<Mission> missions = List.of(courseMission, exploreMission1);

            // when & then
            assertThatThrownBy(
                            () ->
                                    missionCommandService.validateMissionsBelongsToStamp(
                                            stampId, missions))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.getMessage());
        }

        @Test
        @DisplayName("미션 목록이 모두 같은 스탬프에 속한다면 예외가 발생하지 않는다.")
        void shouldValidMissionsWhenAllMissionsBelongToStamp() {
            // given
            Long stampId = exploreStamp.getId();
            List<Mission> missions = List.of(exploreMission1, exploreMission2);

            // when & then
            assertDoesNotThrow(
                    () -> missionCommandService.validateMissionsBelongsToStamp(stampId, missions));
        }
    }

    @Nested
    @DisplayName("validateAllMissionsCompletedByStampId 메서드는")
    class ValidateAllMissionsCompletedByStampId {

        @Test
        @DisplayName("특정 스탬프 하위의 미션이 하나라도 완료되지 않았다면 예외가 발생한다.")
        void shouldThrowExceptionWhenAnyMissionIsNotCompleted() {
            // given
            Long stampId = courseStamp.getId();
            given(
                            missionQueryRepository
                                    .existsByStampIdAndCompletedIsFalseAndDeletedAtIsNull(stampId))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(
                            () ->
                                    missionCommandService.validateAllMissionsCompletedByStampId(
                                            stampId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.ALL_MISSIONS_NOT_COMPLETED.getMessage());
        }

        @Test
        @DisplayName("특정 스탬프 하위의 모든 미션이 완료되면 예외가 발생하지 않는다.")
        void shouldPassWhenAllMissionsAreCompleted() {
            // given
            Long stampId = courseStamp.getId();
            given(
                            missionQueryRepository
                                    .existsByStampIdAndCompletedIsFalseAndDeletedAtIsNull(stampId))
                    .willReturn(false);

            // when & then
            assertDoesNotThrow(
                    () -> missionCommandService.validateAllMissionsCompletedByStampId(stampId));
        }
    }

    @Nested
    @DisplayName("hardDeleteMissions 메서드는")
    class HardDeleteMissions {

        @Test
        @DisplayName("삭제된 미션이 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDeletedMissionsDoNotExist() {
            // given
            given(missionQueryRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L);

            // when
            long result = missionCommandService.hardDeleteMissions();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 미션이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDeletedMissionsExist() {
            // given
            given(missionQueryRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L);

            // when
            long result = missionCommandService.hardDeleteMissions();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteMissionsOwnedByDeletedStamp 메서드는")
    class HardDeleteMissionsOwnedByDeletedStamp {

        @Test
        @DisplayName("삭제된 스탬프가 소유한 미션이 없으면 0을 반환한다.")
        void shouldReturnZeroWhenMissionsOwnedByDeletedStampDoNotExist() {
            // given
            given(missionQueryRepository.deleteAllByDeletedStampOwner()).willReturn(0L);

            // when
            long result = missionCommandService.hardDeleteMissionsOwnedByDeletedStamp();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 스탬프가 소유한 미션이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenMissionsOwnedByDeletedStampExist() {
            // given
            given(missionQueryRepository.deleteAllByDeletedStampOwner()).willReturn(5L);

            // when
            long result = missionCommandService.hardDeleteMissionsOwnedByDeletedStamp();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteMissionsByMember 메서드는")
    class HardDeleteMissionsByMember {

        @Test
        @DisplayName("특정 멤버가 소유한 미션이 없으면 0을 반환한다.")
        void shouldReturnZeroWhenMissionsOwnedByMemberDoNotExist() {
            // given
            Long memberId = 1L;
            given(missionQueryRepository.deleteAllByMemberId(memberId)).willReturn(0L);

            // when
            long result = missionCommandService.hardDeleteMissionsByMember(memberId);

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("특정 멤버가 소유한 미션이 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenMissionsOwnedByMemberExist() {
            // given
            Long memberId = 1L;
            given(missionQueryRepository.deleteAllByMemberId(memberId)).willReturn(5L);

            // when
            long result = missionCommandService.hardDeleteMissionsByMember(memberId);

            // then
            assertThat(result).isEqualTo(5L);
        }
    }
}
