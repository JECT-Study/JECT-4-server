package com.ject.studytrip.mission.domain.repository;

import com.ject.studytrip.mission.domain.model.Mission;
import java.util.List;
import java.util.Optional;

public interface MissionRepository {
    List<Mission> findAllByStampIdOrderByMissionOrder(Long stampId);

    List<Mission> findAllByIdIn(List<Long> ids);

    List<Mission> findAllByStampIdAndDeletedAtIsNullOrderByMissionOrder(Long stampId);

    Optional<Mission> findById(Long id);

    boolean existsByStampIdAndMissionOrderAndDeletedAtIsNull(Long stampId, int missionOrder);

    Mission save(Mission mission);
}
