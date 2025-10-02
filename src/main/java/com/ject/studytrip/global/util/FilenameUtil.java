package com.ject.studytrip.global.util;

import java.util.UUID;

public final class FilenameUtil {
    private static final String FILENAME_PATTERN = "%s.%s";

    private FilenameUtil() {}

    public static String createNewFilename(String ext) {
        String newFilename = UUID.randomUUID().toString();
        return FILENAME_PATTERN.formatted(newFilename, ext);
    }

    public static String extractExtension(String filename) {
        if (filename == null) return null;

        int i = filename.lastIndexOf('.');
        if (i < 0 || i == filename.length() - 1) return null;

        return filename.substring(i + 1).toLowerCase();
    }
}
