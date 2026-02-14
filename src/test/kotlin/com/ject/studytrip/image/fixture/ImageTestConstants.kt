package com.ject.studytrip.image.fixture

import com.ject.studytrip.image.domain.constants.ImageConstants.TMP_PREFIX

object ImageTestConstants {
    const val VALID_KEY_PREFIX = "members"
    const val VALID_ID = "12345"
    const val ORIGINAL_FILENAME = "test.jpg"
    const val INVALID_ORIGINAL_FILENAME = "test.txt"
    const val TMP_KEY = TMP_PREFIX + "members/12345/test.jpg"
    const val FINAL_KEY = "members/12345/test.jpg"

    // 크기 관련
    const val VALID_CONTENT_LENGTH = 1024L

    // MIME 타입
    const val VALID_MIME = "image/jpeg"
    const val INVALID_MIME = "text/plain"

    // URL
    const val PRESIGNED_URL = "https://s3.amazonaws.com/test-presigned-url"

    // 바이트 데이터
    val JPEG_HEADER_BYTES = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
}
