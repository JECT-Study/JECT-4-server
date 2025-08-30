package com.ject.studytrip.mission.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.mission.domain.error.MissionErrorCode;
import com.ject.studytrip.mission.domain.factory.MissionFactory;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.policy.MissionPolicy;
import com.ject.studytrip.mission.domain.repository.MissionQueryRepository;
import com.ject.studytrip.mission.domain.repository.MissionRepository;
import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest;
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest;
import com.ject.studytrip.stamp.domain.model.Stamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionService {
    private final MissionRepository missionRepository;
    private final MissionQueryRepository missionQueryRepository;

    @Transactional
    public Mission createMission(Stamp stamp, CreateMissionRequest request) {
        Mission mission = MissionFactory.create(stamp, request.missionName());

        return missionRepository.save(mission);
    }

    @Transactional
    public void updateMissionNameIfPresent(
            Long stampId, Mission mission, UpdateMissionRequest request) {
        validateMissionIsActiveAndBelongsToStamp(stampId, mission);

        mission.updateName(request.missionName());
    }

    @Transactional
    public void deleteMission(Long stampId, Mission mission) {
        validateMissionIsActiveAndBelongsToStamp(stampId, mission);

        mission.updateDeletedAt();
    }

    @Transactional(readOnly = true)
    public List<Mission> getMissionsByStampId(Long stampId) {
        return missionRepository.findAllByStampIdAndDeletedAtIsNullOrderByCreatedAt(stampId);
    }

    @Transactional(readOnly = true)
    public Mission getValidMission(Long stampId, Long missionId) {
        Mission mission =
                missionRepository
                        .findById(missionId)
                        .orElseThrow(() -> new CustomException(MissionErrorCode.MISSION_NOT_FOUND));

        validateMissionIsActiveAndBelongsToStamp(stampId, mission);

        return mission;
    }

    public List<Mission> getValidMissionsWithStamp(List<Long> missionIds) {
        List<Mission> missions = missionQueryRepository.findAllByIdsInFetchJoinStamp(missionIds);

        MissionPolicy.validateExistAll(missions, missionIds);
        missions.forEach(
                mission -> {
                    MissionPolicy.validateNotDeleted(mission);
                    MissionPolicy.validateCompleted(mission);
                });

        return missions;
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public void validateAllMissionsCompletedByStampId(Long stampId) {
        boolean exists =
                missionQueryRepository.existsByStampIdAndCompletedIsFalseAndDeletedAtIsNull(
                        stampId);
        MissionPolicy.validateAllCompleted(exists);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long hardDeleteMissions() {
        return missionQueryRepository.deleteAllByDeletedAtIsNotNull();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long hardDeleteMissionsOwnedByDeletedStamp() {
        return missionQueryRepository.deleteAllByDeletedStampOwner();
    }

    private void validateMissionIsActiveAndBelongsToStamp(Long stampId, Mission mission) {
        MissionPolicy.validateMissionBelongsToStamp(stampId, mission);
        MissionPolicy.validateNotDeleted(mission);
    }
}
