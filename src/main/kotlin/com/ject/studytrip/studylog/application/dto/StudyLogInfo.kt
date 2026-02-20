package com.ject.studytrip.studylog.application.dto

import com.ject.studytrip.global.util.DateUtil
import com.ject.studytrip.global.util.EntityExtensions.requireId
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
        fun from(studyLog: StudyLog): StudyLogInfo =
            StudyLogInfo(
                studyLog.id.requireId(),
                studyLog.title,
                studyLog.content,
                studyLog.imageUrl,
                DateUtil.formatDateTime(requireNotNull(studyLog.createdAt)),
                DateUtil.formatDateTime(requireNotNull(studyLog.createdAt)),
                studyLog.deletedAt?.let { DateUtil.formatDateTime(it) },
            )
    }
}
