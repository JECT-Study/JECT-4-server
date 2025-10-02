package com.ject.studytrip.image.fixture;

import static com.ject.studytrip.image.domain.constants.ImageConstants.*;

public class ImageTestConstants {
    public static final String VALID_KEY_PREFIX = "members";
    public static final String VALID_ID = "12345";
    public static final String ORIGINAL_FILENAME = "test.jpg";
    public static final String INVALID_ORIGINAL_FILENAME = "test.txt";
    public static final String TMP_KEY = TMP_PREFIX + "members/12345/test.jpg";
    public static final String FINAL_KEY = "members/12345/test.jpg";

    // 크기 관련 (ImageConstants와 연동)
    public static final long VALID_CONTENT_LENGTH = 1024L;

    // MIME 타입
    public static final String VALID_MIME = "image/jpeg";
    public static final String INVALID_MIME = "text/plain";

    // URL
    public static final String PRESIGNED_URL = "https://s3.amazonaws.com/test-presigned-url";

    // 바이트 데이터
    public static final byte[] JPEG_HEADER_BYTES =
            new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}; // JPEG header
}
