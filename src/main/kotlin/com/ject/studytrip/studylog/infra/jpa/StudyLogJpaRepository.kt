package com.ject.studytrip.studylog.infra.jpa

import com.ject.studytrip.studylog.domain.model.StudyLog
import org.springframework.data.jpa.repository.JpaRepository

interface StudyLogJpaRepository : JpaRepository<StudyLog, Long> {
    fun findAllByIdIn(studyLogIds: List<Long>): List<StudyLog>
}
