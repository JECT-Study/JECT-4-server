package com.ject.studytrip.image.infra.tika.provider;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.image.infra.tika.error.TikaErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TikaImageProbeProvider {
    private final Tika tika;

    public String detectMime(byte[] headBytes) {
        try {
            return tika.detect(headBytes);
        } catch (Exception e) {
            log.error("Tika Exception: {}", e.getMessage(), e);
            throw new CustomException(TikaErrorCode.TIKA_SERVER_ERROR);
        }
    }
}
