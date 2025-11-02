package com.ject.studytrip.mission.domain.repository;

import com.ject.studytrip.mission.domain.model.DailyMission;
import java.util.List;

public interface DailyMissionQueryRepository {
    List<DailyMission> findAllByDailyGoalIdFetchJoinMission(Long dailyGoalId);

    List<DailyMission> findAllWithMissionAndStampByIds(List<Long> ids);
}
