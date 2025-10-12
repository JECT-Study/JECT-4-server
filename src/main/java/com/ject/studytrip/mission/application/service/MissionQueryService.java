package com.ject.studytrip.mission.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.mission.domain.error.MissionErrorCode;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.policy.MissionPolicy;
import com.ject.studytrip.mission.domain.repository.MissionQueryRepository;
import com.ject.studytrip.mission.domain.repository.MissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MissionQueryService {
    private final MissionRepository missionRepository;
    private final MissionQueryRepository missionQueryRepository;

    public Mission getValidMission(Long stampId, Long missionId) {
        Mission mission =
                missionRepository
                        .findById(missionId)
                        .orElseThrow(() -> new CustomException(MissionErrorCode.MISSION_NOT_FOUND));

        MissionPolicy.validateMissionBelongsToStamp(stampId, mission);
        MissionPolicy.validateNotDeleted(mission);

        return mission;
    }

    public List<Mission> getMissionsByStampId(Long stampId) {
        return missionRepository.findAllByStampIdAndDeletedAtIsNullOrderByCreatedAt(stampId);
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

    public long countCompletedMissionsByTripId(Long tripId) {
        return missionQueryRepository.countCompletedMissionsByTripId(tripId);
    }
}
