package com.ject.studytrip.image.fixture;

import static com.ject.studytrip.image.domain.constants.ImageConstants.MAX_IMAGE_SIZE_BYTES;

import com.ject.studytrip.image.infra.s3.dto.ImageHeadInfo;

public class ImageHeadInfoFixture {
    private static final long DEFAULT_CONTENT_LENGTH = 1024L;
    private static final long LARGE_CONTENT_LENGTH = MAX_IMAGE_SIZE_BYTES + 1;
    private static final long EMPTY_CONTENT_LENGTH = 0L;

    public static ImageHeadInfo createImageHeadInfo() {
        return ImageHeadInfo.of(DEFAULT_CONTENT_LENGTH);
    }

    public static ImageHeadInfo createImageHeadInfo(long contentLength) {
        return ImageHeadInfo.of(contentLength);
    }

    public static ImageHeadInfo createLargeImageHeadInfo() {
        return ImageHeadInfo.of(LARGE_CONTENT_LENGTH);
    }

    public static ImageHeadInfo createEmptyImageHeadInfo() {
        return ImageHeadInfo.of(EMPTY_CONTENT_LENGTH);
    }
}
