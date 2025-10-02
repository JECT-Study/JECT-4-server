package com.ject.studytrip.image.domain.factory;

import static com.ject.studytrip.image.domain.constants.ImageConstants.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageKeyFactory {
    public static String createTmpKey(String keyPrefix, String id, String filename) {
        return TMP_PREFIX + KEY_PATTERN.formatted(keyPrefix, id, filename);
    }

    public static String toFinalKey(String tmpKey) {
        if (tmpKey == null || tmpKey.isBlank() || !tmpKey.startsWith(TMP_PREFIX)) {
            return null;
        }
        return tmpKey.substring(TMP_PREFIX.length());
    }
}
