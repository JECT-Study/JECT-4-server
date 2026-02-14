package com.ject.studytrip.image.infra.tika.provider

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.image.infra.tika.error.TikaErrorCode
import org.apache.tika.Tika
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TikaImageProbeProvider(
    private val tika: Tika,
) {
    companion object {
        private val log = LoggerFactory.getLogger(TikaImageProbeProvider::class.java)
    }

    fun detectMime(headBytes: ByteArray): String =
        try {
            tika.detect(headBytes)
        } catch (e: Exception) {
            log.error("Tika Exception: {}", e.message, e)
            throw CustomException(TikaErrorCode.TIKA_SERVER_ERROR)
        }
}
