package com.ject.studytrip.member.application.dto

data class PresignedProfileImageInfo(
    val memberId: Long,
    val tmpKey: String,
    val presignedUrl: String,
)
