package com.ject.studytrip.mission.infra.jpa;

import com.ject.studytrip.mission.domain.model.Mission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionJpaRepository extends JpaRepository<Mission, Long> {
    List<Mission> findAllByIdIn(List<Long> ids);

    List<Mission> findAllByStampIdAndDeletedAtIsNullOrderByCreatedAt(Long stampId);
}
