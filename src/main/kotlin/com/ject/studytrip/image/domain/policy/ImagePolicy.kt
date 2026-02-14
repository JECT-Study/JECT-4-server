package com.ject.studytrip.image.domain.policy

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.image.domain.constants.ImageConstants
import com.ject.studytrip.image.domain.error.ImageErrorCode
import org.springframework.util.StringUtils.hasText

object ImagePolicy {
    fun validateExtension(ext: String?) {
        if (!hasText(ext) || ext !in ImageConstants.ALLOWED_EXTENSIONS) {
            throw CustomException(ImageErrorCode.INVALID_IMAGE_EXTENSION)
        }
    }

    fun validateMime(mime: String?) {
        if (!hasText(mime) || mime !in ImageConstants.ALLOWED_MIME_TYPES) {
            throw CustomException(ImageErrorCode.INVALID_IMAGE_MIME)
        }
    }

    fun validateKeyPrefix(keyPrefix: String?) {
        if (!hasText(keyPrefix) || keyPrefix !in ImageConstants.ALLOWED_OBJECT_KEY_PREFIXES) {
            throw CustomException(ImageErrorCode.INVALID_IMAGE_KEY_PREFIX)
        }
    }

    fun validateKey(key: String?) {
        if (!hasText(key)) {
            throw CustomException(ImageErrorCode.INVALID_IMAGE_KEY)
        }
    }

    fun validateSize(contentLength: Long) {
        if (contentLength < ImageConstants.MIN_IMAGE_SIZE_BYTES) {
            throw CustomException(ImageErrorCode.EMPTY_IMAGE)
        }

        if (contentLength > ImageConstants.MAX_IMAGE_SIZE_BYTES) {
            throw CustomException(ImageErrorCode.IMAGE_SIZE_EXCEEDED)
        }
    }
}
