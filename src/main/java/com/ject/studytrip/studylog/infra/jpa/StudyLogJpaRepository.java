package com.ject.studytrip.studylog.infra.jpa;

import com.ject.studytrip.studylog.domain.model.StudyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyLogJpaRepository extends JpaRepository<StudyLog, Long> {}
