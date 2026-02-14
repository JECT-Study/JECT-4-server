package com.ject.studytrip.image.domain.factory

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.image.domain.constants.ImageConstants
import com.ject.studytrip.image.domain.error.ImageErrorCode

object ImageKeyFactory {
    fun createTmpKey(
        keyPrefix: String?,
        id: String,
        filename: String,
    ): String = ImageConstants.TMP_PREFIX + ImageConstants.KEY_PATTERN.format(keyPrefix, id, filename)

    fun toFinalKey(tmpKey: String): String {
        if (!tmpKey.startsWith(ImageConstants.TMP_PREFIX)) {
            throw CustomException(ImageErrorCode.INVALID_IMAGE_KEY)
        }
        return tmpKey.removePrefix(ImageConstants.TMP_PREFIX)
    }
}
