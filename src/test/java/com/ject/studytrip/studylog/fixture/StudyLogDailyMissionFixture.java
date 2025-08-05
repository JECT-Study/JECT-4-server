package com.ject.studytrip.studylog.fixture;

import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.studylog.domain.factory.StudyLogDailyMissionFactory;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import org.springframework.test.util.ReflectionTestUtils;

public class StudyLogDailyMissionFixture {
    public static StudyLogDailyMission createStudyLogDailyMission(
            StudyLog studyLog, DailyMission dailyMission) {
        return StudyLogDailyMissionFactory.create(studyLog, dailyMission);
    }

    public static StudyLogDailyMission createStudyLogDailyMissionWithId(
            Long id, StudyLog studyLog, DailyMission dailyMission) {
        StudyLogDailyMission studyLogDailyMission =
                createStudyLogDailyMission(studyLog, dailyMission);
        ReflectionTestUtils.setField(studyLogDailyMission, "id", id);

        return studyLogDailyMission;
    }
}
