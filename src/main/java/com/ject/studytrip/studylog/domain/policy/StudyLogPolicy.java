package com.ject.studytrip.studylog.domain.policy;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.studylog.domain.error.StudyLogErrorCode;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StudyLogPolicy {
    public static void validateNotDeleted(StudyLog studyLog) {
        if (studyLog.getDeletedAt() != null) {
            throw new CustomException(StudyLogErrorCode.STUDY_LOG_ALREADY_DELETED);
        }
    }
}
