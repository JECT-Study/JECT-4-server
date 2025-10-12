package com.ject.studytrip.studylog.infra.jpa;

import com.ject.studytrip.studylog.domain.model.StudyLog;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyLogJpaRepository extends JpaRepository<StudyLog, Long> {
    List<StudyLog> findAllByIdIn(Collection<Long> studyLogIds);
}
