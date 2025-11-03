package com.ject.studytrip.image.application.dto;

import java.util.List;

public record CleanupImagesResult(int success, List<String> failedKeys) {
    public static CleanupImagesResult of(int success, List<String> failedKeys) {
        return new CleanupImagesResult(success, failedKeys);
    }
}
