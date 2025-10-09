package com.ject.studytrip.studylog.infra.jpa;

import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.repository.StudyLogRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyLogRepositoryAdapter implements StudyLogRepository {
    private final StudyLogJpaRepository studyLogJpaRepository;

    @Override
    public StudyLog save(StudyLog studyLog) {
        return studyLogJpaRepository.save(studyLog);
    }

    @Override
    public Optional<StudyLog> findById(Long studyLogId) {
        return studyLogJpaRepository.findById(studyLogId);
    }
}
