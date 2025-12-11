package com.ject.studytrip.studylog.infra.jpa

import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.repository.StudyLogRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class StudyLogRepositoryAdapter(
    private val studyLogJpaRepository: StudyLogJpaRepository,
) : StudyLogRepository {
    override fun save(studyLog: StudyLog): StudyLog = studyLogJpaRepository.save(studyLog)

    override fun findById(studyLogId: Long): Optional<StudyLog> = studyLogJpaRepository.findById(studyLogId)

    override fun findAllByIdIn(studyLogIds: Collection<Long>): List<StudyLog> = studyLogJpaRepository.findAllByIdIn(studyLogIds)
}
