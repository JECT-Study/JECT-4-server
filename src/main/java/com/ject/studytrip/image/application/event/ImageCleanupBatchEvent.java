package com.ject.studytrip.image.application.event;

import java.util.List;

public record ImageCleanupBatchEvent(List<String> imageUrls) {
    public static ImageCleanupBatchEvent of(List<String> imageUrls) {
        return new ImageCleanupBatchEvent(imageUrls);
    }
}
