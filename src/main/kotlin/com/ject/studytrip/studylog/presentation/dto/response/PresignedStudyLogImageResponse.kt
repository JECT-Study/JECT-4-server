package com.ject.studytrip.studylog.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class PresignedStudyLogImageResponse(
    @field:Schema(description = "학습 로그 ID")
    val studyLogId: Long,
    @field:Schema(description = "학습 로그 이미지 임시키")
    val tmpKey: String,
    @field:Schema(description = "학습 로그 이미지 업로드용 Presigned URL")
    val presignedUrl: String,
)
