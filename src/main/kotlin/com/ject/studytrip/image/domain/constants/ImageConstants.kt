package com.ject.studytrip.image.domain.constants

import org.springframework.util.unit.DataSize

object ImageConstants {
    val ALLOWED_MIME_TYPES: Set<String> = setOf("image/jpeg", "image/png", "image/webp")

    val ALLOWED_OBJECT_KEY_PREFIXES: Set<String> = setOf("members", "study-logs", "trip-reports")

    val ALLOWED_EXTENSIONS: Set<String> = setOf("jpg", "jpeg", "png", "webp")

    val MAX_IMAGE_SIZE_BYTES: Long = DataSize.ofMegabytes(5).toBytes()

    const val MIN_IMAGE_SIZE_BYTES: Long = 1L

    const val PROBE_BYTES: Int = 32 * 1024 // 32KB

    const val KEY_PATTERN: String = "%s/%s/%s"

    const val TMP_PREFIX: String = "tmp/"
}
