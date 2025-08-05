package com.ject.studytrip.studylog.infra.jpa;

import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyLogDailyMissionJpaRepository
        extends JpaRepository<StudyLogDailyMission, Long> {}
