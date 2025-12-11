package com.ject.studytrip.studylog.domain.repository

import com.ject.studytrip.studylog.domain.model.StudyLog
import java.util.Optional

interface StudyLogRepository {
    fun save(studyLog: StudyLog): StudyLog

    fun findById(studyLogId: Long): Optional<StudyLog>

    fun findAllByIdIn(studyLogIds: Collection<Long>): List<StudyLog>
}
