package com.ject.studytrip.mission.domain.repository;

import com.ject.studytrip.mission.domain.model.Mission;
import java.util.List;
import java.util.Optional;

public interface MissionRepository {
    List<Mission> findAllByIdIn(List<Long> ids);

    List<Mission> findAllByStampIdAndDeletedAtIsNullOrderByCreatedAt(Long stampId);

    Optional<Mission> findById(Long id);

    Mission save(Mission mission);
}
