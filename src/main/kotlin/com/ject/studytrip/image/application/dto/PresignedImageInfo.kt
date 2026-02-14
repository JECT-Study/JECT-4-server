package com.ject.studytrip.image.application.dto

data class PresignedImageInfo(
    val tmpKey: String,
    val presignedUrl: String,
)
