package com.ject.studytrip.image.application.event

import com.ject.studytrip.image.application.service.ImageService
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ImageEventListener(
    private val imageService: ImageService,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleCleanupBatch(event: ImageCleanupBatchEvent) = imageService.cleanupBatch(event.imageUrls)
}
