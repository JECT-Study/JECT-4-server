package com.ject.studytrip.image.application.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishCleanupBatch(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;

        ImageCleanupBatchEvent event = ImageCleanupBatchEvent.of(imageUrls);
        publisher.publishEvent(event);
    }
}
