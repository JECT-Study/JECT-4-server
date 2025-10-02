package com.ject.studytrip.image.domain.constants;

import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.unit.DataSize;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageConstants {

    public static final Set<String> ALLOWED_MIME_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    public static final long MAX_IMAGE_SIZE_BYTES = DataSize.ofMegabytes(5).toBytes();
    public static final long MIN_IMAGE_SIZE_BYTES = 1L;

    public static final int PROBE_BYTES = 32 * 1024; // 32KB

    public static final String KEY_PATTERN = "%s/%s/%s";
    public static final String TMP_PREFIX = "tmp/";
    public static final Set<String> ALLOWED_OBJECT_KEY_PREFIXES =
            Set.of("members", "study-logs", "trip-reports");

    public static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
}
