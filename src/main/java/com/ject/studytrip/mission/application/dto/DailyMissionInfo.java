package com.ject.studytrip.mission.application.dto;

import com.ject.studytrip.mission.domain.model.DailyMission;

public record DailyMissionInfo(Long dailyMissionId, MissionInfo missionInfo) {
    public static DailyMissionInfo from(DailyMission dailyMission) {
        return new DailyMissionInfo(
                dailyMission.getId(), MissionInfo.from(dailyMission.getMission()));
    }
}
