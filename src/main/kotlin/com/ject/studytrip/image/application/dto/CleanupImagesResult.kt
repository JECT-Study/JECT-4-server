package com.ject.studytrip.image.application.dto

data class CleanupImagesResult(
    val success: Int,
    val failedKeys: List<String>,
)
