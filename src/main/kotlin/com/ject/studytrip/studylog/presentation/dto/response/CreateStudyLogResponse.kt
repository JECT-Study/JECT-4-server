package com.ject.studytrip.studylog.presentation.dto.response

import com.ject.studytrip.studylog.application.dto.StudyLogInfo
import io.swagger.v3.oas.annotations.media.Schema

data class CreateStudyLogResponse(
    @field:Schema(description = "생성된 학습 로그 ID")
    val studyLogId: Long,
) {
    companion object {
        @JvmStatic
        fun of(studyLogInfo: StudyLogInfo): CreateStudyLogResponse = CreateStudyLogResponse(studyLogInfo.studyLogId)
    }
}
