package com.ject.studytrip.studylog.application.dto

data class StudyLogSliceInfo(
    val studyLogDetails: List<StudyLogDetail>,
    val hasNext: Boolean,
) {
    companion object {
        @JvmStatic
        fun of(
            studyLogDetails: List<StudyLogDetail>,
            hasNext: Boolean,
        ): StudyLogSliceInfo = StudyLogSliceInfo(studyLogDetails, hasNext)
    }
}
