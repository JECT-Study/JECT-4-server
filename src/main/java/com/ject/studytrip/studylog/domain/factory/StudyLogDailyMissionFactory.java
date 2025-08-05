package com.ject.studytrip.studylog.domain.factory;

import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StudyLogDailyMissionFactory {
    public static StudyLogDailyMission create(StudyLog studyLog, DailyMission dailyMission) {
        return StudyLogDailyMission.of(studyLog, dailyMission);
    }
}
