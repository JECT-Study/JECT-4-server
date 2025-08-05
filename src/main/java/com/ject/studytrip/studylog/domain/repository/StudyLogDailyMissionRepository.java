package com.ject.studytrip.studylog.domain.repository;

import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import java.util.List;

public interface StudyLogDailyMissionRepository {
    List<StudyLogDailyMission> saveAll(List<StudyLogDailyMission> studyLogDailyMissions);
}
