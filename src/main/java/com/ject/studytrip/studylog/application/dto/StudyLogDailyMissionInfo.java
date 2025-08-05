package com.ject.studytrip.studylog.application.dto;

import com.ject.studytrip.mission.application.dto.DailyMissionInfo;
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;

public record StudyLogDailyMissionInfo(
        Long studyLogDailyMissionId, DailyMissionInfo dailyMissionInfo) {
    public static StudyLogDailyMissionInfo from(StudyLogDailyMission studyLogDailyMission) {
        return new StudyLogDailyMissionInfo(
                studyLogDailyMission.getId(),
                DailyMissionInfo.from(studyLogDailyMission.getDailyMission()));
    }
}
