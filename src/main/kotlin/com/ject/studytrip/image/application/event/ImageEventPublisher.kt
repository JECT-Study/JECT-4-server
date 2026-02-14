package com.ject.studytrip.image.application.event

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class ImageEventPublisher(
    private val publisher: ApplicationEventPublisher,
) {
    fun publishCleanupBatch(imageUrls: List<String>?) {
        if (imageUrls.isNullOrEmpty()) return

        publisher.publishEvent(ImageCleanupBatchEvent(imageUrls))
    }
}
