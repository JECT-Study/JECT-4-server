package com.ject.studytrip.mission.infra.jpa;

import com.ject.studytrip.mission.domain.model.Mission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionJpaRepository extends JpaRepository<Mission, Long> {
    List<Mission> findAllByStampIdOrderByMissionOrder(Long stampId);

    List<Mission> findAllByIdIn(List<Long> ids);

    List<Mission> findAllByStampIdAndDeletedAtIsNullOrderByMissionOrder(Long stampId);

    boolean existsByStampIdAndMissionOrderAndDeletedAtIsNull(Long stampId, int missionOrder);
}
