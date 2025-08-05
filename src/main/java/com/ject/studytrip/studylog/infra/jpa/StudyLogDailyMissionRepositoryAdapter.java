package com.ject.studytrip.studylog.infra.jpa;

import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyLogDailyMissionRepositoryAdapter implements StudyLogDailyMissionRepository {
    private final StudyLogDailyMissionJpaRepository studyLogDailyMissionJpaRepository;

    @Override
    public List<StudyLogDailyMission> saveAll(List<StudyLogDailyMission> studyLogDailyMissions) {
        return studyLogDailyMissionJpaRepository.saveAll(studyLogDailyMissions);
    }
}
