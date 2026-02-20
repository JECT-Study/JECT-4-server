package com.ject.studytrip.global.util

import java.util.UUID

object FilenameUtil {
    fun createNewFilename(ext: String): String = "${UUID.randomUUID()}.$ext"

    fun extractExtension(filename: String?): String? =
        filename
            ?.substringAfterLast('.', "")
            ?.takeIf { it.isNotBlank() }
            ?.lowercase()
}
