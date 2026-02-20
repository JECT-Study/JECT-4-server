package com.ject.studytrip.studylog.application.dto

data class StudyLogSliceInfo(
    val studyLogDetails: List<StudyLogDetail>,
    val hasNext: Boolean,
)
