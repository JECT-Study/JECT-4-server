package com.ject.studytrip.image.application.event;

import com.ject.studytrip.image.application.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ImageEventListener {

    private final ImageService imageService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCleanupBatch(ImageCleanupBatchEvent event) {
        imageService.cleanupBatch(event.imageUrls());
    }
}
