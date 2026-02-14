package com.ject.studytrip.image.fixture

import com.ject.studytrip.image.domain.constants.ImageConstants.MAX_IMAGE_SIZE_BYTES
import com.ject.studytrip.image.infra.s3.dto.ImageHeadInfo

class ImageHeadInfoFixture(
    private val defaultContentLength: Long = 1024L,
    private val largeContentLength: Long = MAX_IMAGE_SIZE_BYTES + 1,
    private val emptyContentLength: Long = 0L,
) {
    fun create(): ImageHeadInfo = ImageHeadInfo(defaultContentLength)

    fun create(contentLength: Long): ImageHeadInfo = ImageHeadInfo(contentLength)

    fun createLarge(): ImageHeadInfo = ImageHeadInfo(largeContentLength)

    fun createEmpty(): ImageHeadInfo = ImageHeadInfo(emptyContentLength)
}
