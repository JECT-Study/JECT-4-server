package com.ject.studytrip.mission.infra.jpa;

import com.ject.studytrip.mission.domain.model.DailyMission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyMissionJpaRepository extends JpaRepository<DailyMission, Long> {
    List<DailyMission> findAllByIdIn(List<Long> ids);

    List<DailyMission> findAllByDailyGoalIdAndDeletedAtIsNull(Long dailyGoalId);
}
