package com.ject.studytrip.mission.infra.jpa;

import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.repository.MissionRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionRepositoryAdapter implements MissionRepository {
    private final MissionJpaRepository missionJpaRepository;

    @Override
    public List<Mission> findAllByIdIn(List<Long> ids) {
        return missionJpaRepository.findAllByIdIn(ids);
    }

    @Override
    public List<Mission> findAllByStampIdAndDeletedAtIsNullOrderByCreatedAt(Long stampId) {
        return missionJpaRepository.findAllByStampIdAndDeletedAtIsNullOrderByCreatedAt(stampId);
    }

    @Override
    public Optional<Mission> findById(Long id) {
        return missionJpaRepository.findById(id);
    }

    @Override
    public Mission save(Mission mission) {
        return missionJpaRepository.save(mission);
    }
}
