package com.ject.studytrip.image.infra.s3.dto;

public record ImageHeadInfo(long contentLength) {
    public static ImageHeadInfo of(long contentLength) {
        return new ImageHeadInfo(contentLength);
    }
}
