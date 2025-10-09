package com.ject.studytrip.image.domain.util;

import static org.springframework.util.StringUtils.hasText;

import java.net.URI;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageUrlUtil {
    // 이미지 최종 경로 생성
    public static String build(String baseUrl, String key) {
        if (!hasText(baseUrl) || !hasText(key)) {
            return null;
        }

        String normalizedDomain =
                baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        return normalizedDomain + "/" + key.trim();
    }

    // 이미지 최종 경로에서 이미지 키 추출
    public static Optional<String> extractKey(String baseUrl, String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            return Optional.empty();
        }

        // URI 파싱
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        String host = uri.getHost();
        if (host == null || !host.equals(extractHostName(baseUrl))) {
            return Optional.empty();
        }

        String path = uri.getPath();
        if (path == null || path.length() <= 1) {
            return Optional.empty();
        }

        // 맨 앞의 '/' 제거
        String key = path.startsWith("/") ? path.substring(1) : path;
        return key.isBlank() ? Optional.empty() : Optional.of(key);
    }

    // 경로에서 Host 추출
    private static String extractHostName(String baseUrl) {
        if (baseUrl.startsWith("https://")) {
            return baseUrl.substring(8).replaceAll("/$", "");
        }
        if (baseUrl.startsWith("http://")) {
            return baseUrl.substring(7).replaceAll("/$", "");
        }
        return baseUrl.replaceAll("/$", "");
    }
}
