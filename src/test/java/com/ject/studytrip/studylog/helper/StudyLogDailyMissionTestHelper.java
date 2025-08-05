package com.ject.studytrip.studylog.helper;

import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionRepository;
import com.ject.studytrip.studylog.fixture.StudyLogDailyMissionFixture;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyLogDailyMissionTestHelper {
    @Autowired private StudyLogDailyMissionRepository studyLogDailyMissionRepository;

    public List<StudyLogDailyMission> saveStudyLogDailyMissions(
            StudyLog studyLog, DailyMission dailyMission) {
        StudyLogDailyMission studyLogDailyMission =
                StudyLogDailyMissionFixture.createStudyLogDailyMission(studyLog, dailyMission);
        return studyLogDailyMissionRepository.saveAll(List.of(studyLogDailyMission));
    }
}
