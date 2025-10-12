package com.ject.studytrip.studylog.domain.repository;

import com.ject.studytrip.studylog.domain.model.StudyLog;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudyLogRepository {

    StudyLog save(StudyLog studyLog);

    Optional<StudyLog> findById(Long studyLogId);

    List<StudyLog> findAllByIdIn(Collection<Long> studyLogIds);
}
