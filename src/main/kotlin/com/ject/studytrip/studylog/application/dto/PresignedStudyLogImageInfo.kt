package com.ject.studytrip.studylog.application.dto

data class PresignedStudyLogImageInfo(
    val studyLogId: Long,
    val tmpKey: String,
    val presignedUrl: String,
) {
    companion object {
        @JvmStatic
        fun of(
            studyLogId: Long,
            tmpKey: String,
            presignedUrl: String,
        ): PresignedStudyLogImageInfo =
            PresignedStudyLogImageInfo(
                studyLogId,
                tmpKey,
                presignedUrl,
            )
    }
}
