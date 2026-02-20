package com.ject.studytrip.global.util

import java.net.URI

object OriginArgumentUtil {
    private const val NULL_ORIGIN = "null"
    private const val HTTP = "http"
    private const val HTTPS = "https"
    private const val DEFAULT_HTTP_PORT = 80
    private const val DEFAULT_HTTPS_PORT = 443
    private const val MIN_PORT = 1
    private const val MAX_PORT = 65535

    fun resolveOrigin(
        origin: String?,
        scheme: String,
        host: String,
        port: Int,
    ): String? {
        val headerOrigin =
            origin
                ?.trim()
                ?.takeIf { it.isNotBlank() && !it.equals(NULL_ORIGIN, ignoreCase = true) }

        return canonicalizeOrigin(headerOrigin ?: "$scheme://$host:$port")
    }

    private fun canonicalizeOrigin(raw: String?): String? {
        val trimmed = raw?.trim().takeUnless { it.isNullOrBlank() } ?: return null

        val uri = runCatching { URI.create(trimmed) }.getOrNull() ?: return null

        val scheme =
            uri.scheme
                ?.lowercase()
                ?.takeIf { it == HTTP || it == HTTPS }
                ?: return null

        val host = uri.host ?: return null

        val port = uri.port
        if (port != -1 && port !in MIN_PORT..MAX_PORT) return null

        return buildString {
            append(scheme)
            append("://")
            append(host)

            if (port != -1 && !isDefaultPort(scheme, port)) {
                append(":")
                append(port)
            }
        }
    }

    private fun isDefaultPort(
        scheme: String,
        port: Int,
    ): Boolean = (scheme == HTTP && port == DEFAULT_HTTP_PORT) || (scheme == HTTPS && port == DEFAULT_HTTPS_PORT)
}
