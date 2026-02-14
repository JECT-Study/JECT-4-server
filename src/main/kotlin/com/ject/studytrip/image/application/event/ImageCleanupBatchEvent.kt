package com.ject.studytrip.image.application.event

data class ImageCleanupBatchEvent(
    val imageUrls: List<String>,
)
