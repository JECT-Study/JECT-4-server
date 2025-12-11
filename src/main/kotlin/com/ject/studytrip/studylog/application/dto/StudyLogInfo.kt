package com.ject.studytrip.studylog.application.dto

import com.ject.studytrip.global.util.DateUtil
import com.ject.studytrip.studylog.domain.model.StudyLog

data class StudyLogInfo(
    val studyLogId: Long,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
) {
    companion object {
        @JvmStatic
        fun from(studyLog: StudyLog): StudyLogInfo =
            StudyLogInfo(
                studyLog.getId(),
                studyLog.getTitle(),
                studyLog.getContent(),
                studyLog.getImageUrl(),
                DateUtil.formatDateTime(studyLog.getCreatedAt()),
                DateUtil.formatDateTime(studyLog.getUpdatedAt()),
                studyLog.getDeletedAt()?.let { DateUtil.formatDateTime(it) },
            )
    }
}
