package com.ject.studytrip.mission.infra.jpa;

import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.repository.DailyMissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DailyMissionRepositoryAdapter implements DailyMissionRepository {
    private final DailyMissionJpaRepository dailyMissionJpaRepository;

    @Override
    public DailyMission save(DailyMission dailyMission) {
        return dailyMissionJpaRepository.save(dailyMission);
    }

    @Override
    public List<DailyMission> saveAll(List<DailyMission> dailyMissions) {
        return dailyMissionJpaRepository.saveAll(dailyMissions);
    }

    @Override
    public List<DailyMission> findAllByIdIn(List<Long> ids) {
        return dailyMissionJpaRepository.findAllByIdIn(ids);
    }
}
