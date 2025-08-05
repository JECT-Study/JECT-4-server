package com.ject.studytrip.studylog.application.dto;

import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import java.util.List;

public record StudyLogDetail(
        StudyLogInfo studyLogInfo, List<StudyLogDailyMissionInfo> studyLogDailyMissionInfos) {
    public static StudyLogDetail from(
            StudyLog studyLog, List<StudyLogDailyMission> studyLogDailyMissions) {
        return new StudyLogDetail(
                StudyLogInfo.from(studyLog),
                studyLogDailyMissions.stream().map(StudyLogDailyMissionInfo::from).toList());
    }
}
