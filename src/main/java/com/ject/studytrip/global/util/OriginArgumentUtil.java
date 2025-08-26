package com.ject.studytrip.global.util;

import static org.springframework.util.StringUtils.hasText;

import java.net.URI;

public final class OriginArgumentUtil {
    private static final String NULL_ORIGIN = "null";
    private static final String HTTP_SCHEME = "http";
    private static final String HTTPS_SCHEME = "https";
    private static final String SCHEME_HOST_SEPARATOR = "://";
    private static final String HOST_PORT_SEPARATOR = ":";
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    private OriginArgumentUtil() {}

    // Origin 추출
    public static String resolveOrigin(String origin, String scheme, String host, int port) {
        // Origin 헤더 정보가 있을 경우
        if (hasText(origin)) {
            if (!NULL_ORIGIN.equalsIgnoreCase(origin.trim())) {
                return canonicalizeOrigin(origin);
            }
        }

        // 서버 정보로 구성
        String serverDerivedOrigin =
                scheme + SCHEME_HOST_SEPARATOR + host + HOST_PORT_SEPARATOR + port;
        return canonicalizeOrigin(serverDerivedOrigin);
    }

    private static String canonicalizeOrigin(String origin) {
        if (!hasText(origin)) return null;

        try {
            URI uri = URI.create(origin.trim());

            String scheme = toSupportedScheme(uri.getScheme());
            if (!hasText(scheme)) return null;

            String host = uri.getHost();
            if (!hasText(host)) return null;

            int port = uri.getPort();
            if (port != -1 && !isValidPort(port)) return null;

            String result = scheme + SCHEME_HOST_SEPARATOR + host;
            if (port != -1 && !isDefaultPort(scheme, port)) {
                result += HOST_PORT_SEPARATOR + port;
            }

            return result;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String toSupportedScheme(String scheme) {
        if (!hasText(scheme)) return null;

        String lower = scheme.trim().toLowerCase();
        if (HTTP_SCHEME.equals(lower) || HTTPS_SCHEME.equals(lower)) {
            return lower;
        }

        return null;
    }

    private static boolean isValidPort(int port) {
        return port >= MIN_PORT && port <= MAX_PORT;
    }

    private static boolean isDefaultPort(String scheme, int port) {
        return (HTTP_SCHEME.equals(scheme) && port == DEFAULT_HTTP_PORT)
                || (HTTPS_SCHEME.equals(scheme) && port == DEFAULT_HTTPS_PORT);
    }
}
