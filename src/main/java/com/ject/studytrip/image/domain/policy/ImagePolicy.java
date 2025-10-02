package com.ject.studytrip.image.domain.policy;

import static com.ject.studytrip.image.domain.constants.ImageConstants.*;
import static org.springframework.util.StringUtils.hasText;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.image.domain.error.ImageErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImagePolicy {

    public static void validateExtension(String ext) {
        if (!hasText(ext) || !ALLOWED_EXTENSIONS.contains(ext)) {
            throw new CustomException(ImageErrorCode.INVALID_IMAGE_EXTENSION);
        }
    }

    public static void validateMime(String mime) {
        if (!hasText(mime) || !ALLOWED_MIME_TYPES.contains(mime)) {
            throw new CustomException(ImageErrorCode.INVALID_IMAGE_MIME);
        }
    }

    public static void validateSize(long contentLength) {
        if (contentLength < MIN_IMAGE_SIZE_BYTES) {
            throw new CustomException(ImageErrorCode.EMPTY_IMAGE);
        }

        if (contentLength > MAX_IMAGE_SIZE_BYTES) {
            throw new CustomException(ImageErrorCode.IMAGE_SIZE_EXCEEDED);
        }
    }

    public static void validateKeyPrefix(String keyPrefix) {
        if (!hasText(keyPrefix) || !ALLOWED_OBJECT_KEY_PREFIXES.contains(keyPrefix)) {
            throw new CustomException(ImageErrorCode.INVALID_IMAGE_KEY_PREFIX);
        }
    }

    public static void validateKey(String key) {
        if (!hasText(key)) {
            throw new CustomException(ImageErrorCode.INVALID_IMAGE_KEY);
        }
    }
}
