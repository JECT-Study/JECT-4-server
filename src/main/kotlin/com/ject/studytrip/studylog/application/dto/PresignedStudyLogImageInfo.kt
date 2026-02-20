package com.ject.studytrip.studylog.application.dto

data class PresignedStudyLogImageInfo(
    val studyLogId: Long,
    val tmpKey: String,
    val presignedUrl: String,
)
