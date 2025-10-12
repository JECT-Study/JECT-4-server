package com.ject.studytrip.studylog.application.dto;

import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record StudyLogDetail(
        StudyLogInfo studyLogInfo, List<StudyLogDailyMissionInfo> studyLogDailyMissionInfos) {
    public static StudyLogDetail from(
            StudyLog studyLog, List<StudyLogDailyMission> studyLogDailyMissions) {
        List<StudyLogDailyMission> safeStudyLogDailyMissions =
                Optional.ofNullable(studyLogDailyMissions).orElse(Collections.emptyList());

        return new StudyLogDetail(
                StudyLogInfo.from(studyLog),
                safeStudyLogDailyMissions.stream().map(StudyLogDailyMissionInfo::from).toList());
    }
}
