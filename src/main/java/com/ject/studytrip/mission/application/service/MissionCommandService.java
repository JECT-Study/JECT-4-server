package com.ject.studytrip.mission.application.service;

import com.ject.studytrip.mission.domain.factory.MissionFactory;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.policy.MissionPolicy;
import com.ject.studytrip.mission.domain.repository.MissionCommandRepository;
import com.ject.studytrip.mission.domain.repository.MissionRepository;
import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest;
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest;
import com.ject.studytrip.stamp.domain.model.Stamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MissionCommandService {
    private final MissionRepository missionRepository;
    private final MissionCommandRepository missionCommandRepository;

    public Mission createMission(Stamp stamp, CreateMissionRequest request) {
        Mission mission = MissionFactory.create(stamp, request.missionName());

        return missionRepository.save(mission);
    }

    public void updateMissionNameIfPresent(Mission mission, UpdateMissionRequest request) {
        mission.updateName(request.missionName());
    }

    public void deleteMission(Mission mission) {
        mission.updateDeletedAt();
    }

    public void completeMission(Mission mission) {
        MissionPolicy.validateNotDeleted(mission);
        MissionPolicy.validateCompleted(mission);

        mission.updateCompleted();
    }

    public void validateMissionsBelongsToStamp(Long stampId, List<Mission> missions) {
        for (Mission mission : missions) {
            MissionPolicy.validateMissionBelongsToStamp(stampId, mission);
        }
    }

    public void validateAllMissionsCompletedByStampId(Long stampId) {
        boolean exists =
                missionCommandRepository.existsByStampIdAndCompletedIsFalseAndDeletedAtIsNull(
                        stampId);
        MissionPolicy.validateAllCompleted(exists);
    }

    public long hardDeleteMissions() {
        return missionCommandRepository.deleteAllByDeletedAtIsNotNull();
    }

    public long hardDeleteMissionsOwnedByDeletedStamp() {
        return missionCommandRepository.deleteAllByDeletedStampOwner();
    }

    public long hardDeleteMissionsByMember(Long memberId) {
        return missionCommandRepository.deleteAllByMemberId(memberId);
    }
}
