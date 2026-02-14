package com.ject.studytrip.image.domain.util

import java.net.URI

object ImageUrlUtil {
    // 이미지 최종 경로 생성
    fun build(
        baseUrl: String,
        key: String,
    ): String {
        require(baseUrl.isNotBlank())
        require(key.isNotBlank())

        val normalizedDomain = baseUrl.removeSuffix("/")
        return "$normalizedDomain/${key.trim()}"
    }

    // 이미지 URL에서 이미지 key 추출
    fun extractKey(
        baseUrl: String?,
        url: String?,
    ): String? {
        if (baseUrl.isNullOrBlank() || url.isNullOrBlank()) {
            return null
        }

        val uri =
            try {
                URI.create(url)
            } catch (_: IllegalArgumentException) {
                return null
            }

        val baseHost = extractHostName(baseUrl) ?: return null
        if (uri.host != baseHost) {
            return null
        }

        val path = uri.path ?: return null
        if (path.length <= 1) {
            return null
        }

        return path
            .removePrefix("/")
            .takeIf { it.isNotBlank() }
    }

    // baseUrl에서 host 추출
    private fun extractHostName(baseUrl: String): String? =
        try {
            URI.create(baseUrl).host
        } catch (_: IllegalArgumentException) {
            null
        }
}
