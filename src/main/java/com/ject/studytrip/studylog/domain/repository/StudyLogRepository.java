package com.ject.studytrip.studylog.domain.repository;

import com.ject.studytrip.studylog.domain.model.StudyLog;
import java.util.Optional;

public interface StudyLogRepository {

    StudyLog save(StudyLog studyLog);

    Optional<StudyLog> findById(Long studyLogId);
}
