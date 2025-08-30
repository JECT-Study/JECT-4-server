package com.ject.studytrip.cleanup.application.executor;

import java.util.function.LongSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HardDeleteExecutor {

    public long run(String phase, LongSupplier supplier) {
        try {
            long deleted = supplier.getAsLong();
            log.info("[HardDelete] phase={}, deleted={}", phase, deleted);
            return deleted;
        } catch (Exception e) {
            log.error("[HardDelete] phase={} failed: {}", phase, e.getMessage(), e);
            return -1L; // 실패 표식
        }
    }
}
