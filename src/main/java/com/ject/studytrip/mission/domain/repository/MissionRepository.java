package com.ject.studytrip.mission.domain.repository;

import com.ject.studytrip.mission.domain.model.Mission;
import java.util.List;

public interface MissionRepository {
    List<Mission> findAllByStampIdOrderByMissionOrder(Long stampId);
}
