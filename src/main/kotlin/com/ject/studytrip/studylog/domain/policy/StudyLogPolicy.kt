package com.ject.studytrip.studylog.domain.policy

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.studylog.domain.error.StudyLogErrorCode
import com.ject.studytrip.studylog.domain.model.StudyLog

object StudyLogPolicy {
    fun validateNotDeleted(studyLog: StudyLog) {
        if (studyLog.isDeleted()) {
            throw CustomException(StudyLogErrorCode.STUDY_LOG_ALREADY_DELETED)
        }
    }

    fun validateExistAll(
        foundStudyLogs: List<StudyLog>,
        requestedIds: List<Long>,
    ) {
        if (foundStudyLogs.size != requestedIds.size) {
            throw CustomException(StudyLogErrorCode.STUDY_LOG_NOT_FOUND)
        }
    }
}
