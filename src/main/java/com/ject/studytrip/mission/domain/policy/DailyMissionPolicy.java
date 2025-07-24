package com.ject.studytrip.mission.domain.policy;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.mission.domain.error.DailyMissionErrorCode;
import com.ject.studytrip.mission.domain.model.DailyMission;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyMissionPolicy {
    public static void validateExistAll(
            List<DailyMission> foundDailyMissions, List<Long> requestedIds) {
        boolean isEquals = foundDailyMissions.size() == requestedIds.size();
        if (!isEquals) throw new CustomException(DailyMissionErrorCode.DAILY_MISSION_NOT_FOUND);
    }

    public static void validateBelongsToDailyGoal(DailyMission dailyMission, Long dailyGoalId) {
        if (!dailyMission.getDailyGoal().getId().equals(dailyGoalId))
            throw new CustomException(DailyMissionErrorCode.DAILY_MISSION_NOT_BELONG_TO_DAILY_GOAL);
    }

    public static void validateNotDeleted(DailyMission dailyMission) {
        if (dailyMission.getDeletedAt() != null)
            throw new CustomException(DailyMissionErrorCode.DAILY_MISSION_ALREADY_DELETED);
    }
}
