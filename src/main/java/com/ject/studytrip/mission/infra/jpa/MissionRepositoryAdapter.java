package com.ject.studytrip.mission.infra.jpa;

import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.repository.MissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionRepositoryAdapter implements MissionRepository {
    private final MissionJpaRepository missionJpaRepository;

    @Override
    public List<Mission> findAllByStampIdOrderByMissionOrder(Long stampId) {
        return missionJpaRepository.findAllByStampIdOrderByMissionOrder(stampId);
    }
}
