package com.ject.studytrip.mission.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("MissionService 단위 테스트")
class MissionServiceTest extends BaseUnitTest {
    private static final String NEW_MISSION_NAME = "NEW MISSION NAME";
    private static final String NEW_MISSION_MEMO = "NEW MISSION MEMO";

    @InjectMocks private MissionService missionService;
    @Mock private MissionRepository missionRepository;
    @Mock private MissionQueryRepository missionQueryRepository;

    private Trip courseTrip;
    private Trip exploreTrip;
    private Stamp courseStamp;
    private Stamp exploreStamp;
    private Mission courseMission;
    private Mission exploreMission1;
    private Mission exploreMission2;

    @BeforeEach
    void setUp() {
        Member member = MemberFixture.createMemberFromKakao();
        courseTrip = TripFixture.createTripWithId(1L, member, TripCategory.COURSE);
        exploreTrip = TripFixture.createTripWithId(2L, member, TripCategory.EXPLORE);
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
            Mission result = missionService.createMission(courseStamp, request);

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
            Mission result = missionService.createMission(exploreStamp, request);

            // then
            assertThat(result).isEqualTo(exploreMission1);
            assertThat(result.getStamp()).isEqualTo(exploreStamp);
        }
    }

    @Nested
    @DisplayName("updateMissionNameAndMemoIfPresent 메서드는")
    class UpdateMissionNameAndMemoIfPresent {

        @Test
        @DisplayName("미션이 다른 스탬프에 속하면 예외가 발생한다.")
        void shouldThrowExceptionWhenMissionNotBelongToStamp() {
            // given
            Long invalidStampId = exploreStamp.getId();
            UpdateMissionRequest request =
                    new UpdateMissionRequestFixture().withName(NEW_MISSION_NAME).build();

            // when & then
            assertThatThrownBy(
                            () ->
                                    missionService.updateMissionNameIfPresent(
                                            invalidStampId, courseMission, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.getMessage());
        }

        @Test
        @DisplayName("미션이 이미 삭제된 경우 예외가 발생한다.")
        void shouldThrowExceptionWhenMissionIsDeleted() {
            // given
            Long stampId = courseStamp.getId();
            ReflectionTestUtils.setField(courseMission, "deletedAt", LocalDateTime.now());
            UpdateMissionRequest request =
                    new UpdateMissionRequestFixture().withName(NEW_MISSION_NAME).build();

            // when & then
            assertThatThrownBy(
                            () ->
                                    missionService.updateMissionNameIfPresent(
                                            stampId, courseMission, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("특정 미션의 이름을 수정하고 DB에 반영한다.")
        void shouldUpdateMissionName() {
            // given
            Long stampId = courseStamp.getId();
            UpdateMissionRequest request =
                    new UpdateMissionRequestFixture().withName(NEW_MISSION_NAME).build();

            // when
            missionService.updateMissionNameIfPresent(stampId, courseMission, request);

            // then
            assertThat(courseMission.getName()).isEqualTo(NEW_MISSION_NAME);
        }
    }

    @Nested
    @DisplayName("deleteMission 메서드는")
    class DeleteMission {

        @Test
        @DisplayName("미션이 다른 스탬프에 속하면 예외가 발생한다.")
        void shouldThrowExceptionWhenMissionNotBelongToStamp() {
            // given
            Long invalidStampId = courseStamp.getId();

            // when & then
            assertThatThrownBy(() -> missionService.deleteMission(invalidStampId, exploreMission1))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.getMessage());
        }

        @Test
        @DisplayName("미션이 이미 삭제된 경우 예외가 발생한다.")
        void shouldThrowExceptionWhenMissionIsDeleted() {
            // given
            Long stampId = exploreStamp.getId();
            ReflectionTestUtils.setField(exploreMission1, "deletedAt", LocalDateTime.now());

            // when & then
            assertThatThrownBy(() -> missionService.deleteMission(stampId, exploreMission1))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("미션을 삭제하면 deletedAt 필드에 현재 시각이 설정된다.")
        void shouldDeleteMission() {
            // given
            Long stampId = courseStamp.getId();
            assertThat(courseMission.getDeletedAt()).isNull();
            LocalDateTime beforeDeletionTime = LocalDateTime.now();

            // when
            missionService.deleteMission(stampId, courseMission);

            // then
            assertThat(courseMission.getDeletedAt()).isNotNull();
            assertThat(courseMission.getDeletedAt()).isAfterOrEqualTo(beforeDeletionTime);
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
            List<Mission> result = missionService.getMissionsByStampId(stampId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(exploreMission1, exploreMission2);
        }
    }

    @Nested
    @DisplayName("getValidMission 메서드는")
    class GetValidMission {

        @Test
        @DisplayName("미션이 존재하지 않으면 예외가 발생한다.")
        void shouldThrowExceptionWhenMissionNotFound() {
            // given
            Long missionId = 99L;
            Long stampId = exploreStamp.getId();
            given(missionRepository.findById(missionId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> missionService.getValidMission(stampId, missionId))
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
            assertThatThrownBy(() -> missionService.getValidMission(invalidStampId, missionId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP.getMessage());
        }

        @Test
        @DisplayName("미션이 이미 삭제된 경우 예외가 발생한다.")
        void shouldThrowExceptionWhenMissionIsDeleted() {
            // given
            Long missionId = exploreMission1.getId();
            Long stampId = exploreStamp.getId();
            ReflectionTestUtils.setField(exploreMission1, "deletedAt", LocalDateTime.now());
            given(missionRepository.findById(missionId)).willReturn(Optional.of(exploreMission1));

            // when & then
            assertThatThrownBy(() -> missionService.getValidMission(stampId, missionId))
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
            Mission result = missionService.getValidMission(stampId, missionId);

            // then
            assertThat(result).isEqualTo(exploreMission1);
        }

        @Test
        @DisplayName("유효한 미션 ID들로 요청 시 검증을 통과하고 미션 리스트를 반환한다")
        void shouldReturnValidMissions() {
            // given
            List<Long> missionIds = List.of(courseMission.getId());
            given(missionQueryRepository.findAllByIdsInFetchJoinStamp(missionIds))
                    .willReturn(List.of(courseMission));

            // when
            List<Mission> result = missionService.getValidMissionsWithStamp(missionIds);

            // then
            assertThat(result).containsExactly(courseMission);
        }

        @Test
        @DisplayName("요청한 ID 개수와 조회된 미션 개수가 다르면 예외가 발생한다")
        void shouldThrowExceptionWhenSomeMissionsDoNotExist() {
            // given
            List<Long> missionIds = List.of(1L, 2L);
            given(missionQueryRepository.findAllByIdsInFetchJoinStamp(missionIds))
                    .willReturn(List.of(courseMission));

            // when & then
            assertThatThrownBy(() -> missionService.getValidMissionsWithStamp(missionIds))
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
            assertThatThrownBy(() -> missionService.getValidMissionsWithStamp(List.of(1L)))
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
            assertThatThrownBy(() -> missionService.getValidMissionsWithStamp(List.of(1L)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_ALREADY_COMPLETED.getMessage());
        }
    }

    @Nested
    @DisplayName("completeMission 메서드는")
    class CompleteMission {

        @Test
        @DisplayName("정상적인 미션이면 completed 필드를 true로 업데이트한다")
        void shouldCompletedMission() {
            // when
            missionService.completeMission(exploreMission1);

            // then
            assertThat(exploreMission1.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("삭제된 미션이면 예외가 발생한다")
        void shouldThrowExceptionWhenMissionIsDeleted() {
            // given
            ReflectionTestUtils.setField(exploreMission1, "deletedAt", LocalDateTime.now());

            // then
            assertThatThrownBy(() -> missionService.completeMission(exploreMission1))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("이미 완료된 미션이면 예외가 발생한다")
        void shouldThrowExceptionWhenMissionIsAlreadyCompleted() {
            // given
            ReflectionTestUtils.setField(exploreMission1, "completed", true);

            // then
            assertThatThrownBy(() -> missionService.completeMission(exploreMission1))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MissionErrorCode.MISSION_ALREADY_COMPLETED.getMessage());
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
            assertThatThrownBy(() -> missionService.validateAllMissionsCompletedByStampId(stampId))
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
            assertDoesNotThrow(() -> missionService.validateAllMissionsCompletedByStampId(stampId));
        }
    }
}
