package com.ject.studytrip.studylog.infra.jpa

import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission
import org.springframework.data.jpa.repository.JpaRepository

interface StudyLogDailyMissionJpaRepository : JpaRepository<StudyLogDailyMission, Long>
