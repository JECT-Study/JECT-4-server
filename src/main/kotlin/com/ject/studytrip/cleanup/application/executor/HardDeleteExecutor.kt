package com.ject.studytrip.cleanup.application.executor

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class HardDeleteExecutor {
    companion object {
        private val log = LoggerFactory.getLogger(HardDeleteExecutor::class.java)
    }

    fun run(
        phase: String,
        supplier: () -> Long,
    ): Long =
        try {
            val deleted = supplier()
            log.info("[HardDelete] phase={}, deleted={}", phase, deleted)
            deleted
        } catch (e: Exception) {
            log.error("[HardDelete] phase={} failed: {}", phase, e.message, e)
            -1L // 실패 표식
        }
}
