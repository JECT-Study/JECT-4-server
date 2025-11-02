package com.ject.studytrip.studylog.domain.repository;

import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import java.util.List;
import java.util.Map;

public interface StudyLogDailyMissionQueryRepository {
    Map<Long, List<StudyLogDailyMission>> findStudyLogDailyMissionsGroupedByStudyLogId(
            List<Long> studyLogIds);
}
