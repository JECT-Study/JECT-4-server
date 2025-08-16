package com.ject.studytrip.mission.domain.policy;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.mission.domain.error.MissionErrorCode;
import com.ject.studytrip.mission.domain.model.Mission;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MissionPolicy {
    public static void validateMissionBelongsToStamp(Long stampId, Mission mission) {
        if (!mission.getStamp().getId().equals(stampId)) {
            throw new CustomException(MissionErrorCode.MISSION_NOT_BELONGS_TO_STAMP);
        }
    }

    public static void validateNotDeleted(Mission mission) {
        if (mission.getDeletedAt() != null) {
            throw new CustomException(MissionErrorCode.MISSION_ALREADY_DELETED);
        }
    }

    public static void validateCompleted(Mission mission) {
        if (mission.isCompleted())
            throw new CustomException(MissionErrorCode.MISSION_ALREADY_COMPLETED);
    }

    public static void validateExistAll(List<Mission> foundMissions, List<Long> requestedIds) {
        boolean isEquals = foundMissions.size() == requestedIds.size();
        if (!isEquals) {
            throw new CustomException(MissionErrorCode.MISSION_NOT_FOUND);
        }
    }

    public static void validateAllCompleted(boolean exists) {
        if (exists) {
            throw new CustomException(MissionErrorCode.ALL_MISSIONS_NOT_COMPLETED);
        }
    }
}
