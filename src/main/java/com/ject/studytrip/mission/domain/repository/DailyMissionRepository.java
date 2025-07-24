package com.ject.studytrip.mission.domain.repository;

import com.ject.studytrip.mission.domain.model.DailyMission;
import java.util.List;

public interface DailyMissionRepository {
    DailyMission save(DailyMission dailyMission);

    List<DailyMission> saveAll(List<DailyMission> dailyMissions);

    List<DailyMission> findAllByIdIn(List<Long> ids);
}
