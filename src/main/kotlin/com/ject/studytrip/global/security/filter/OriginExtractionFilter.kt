package com.ject.studytrip.global.security.filter

import com.google.common.net.HttpHeaders
import com.ject.studytrip.global.common.constants.UrlConstants
import com.ject.studytrip.global.exception.error.CommonErrorCode
import com.ject.studytrip.global.security.handler.SecurityResponseHandler
import com.ject.studytrip.global.util.OriginArgumentUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class OriginExtractionFilter(
    private val securityResponseHandler: SecurityResponseHandler,
) : OncePerRequestFilter() {
    companion object {
        private const val ATTRIBUTE_NAME = "origin"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val origin = resolveOrigin(request)

        // origin null 검증
        if (origin == null) {
            securityResponseHandler.sendResponse(response, CommonErrorCode.INVALID_ORIGIN)
            return
        }

        // 허용된 origin 검증
        if (origin !in UrlConstants.CORS_DOMAINS) {
            securityResponseHandler.sendResponse(response, CommonErrorCode.UNSUPPORTED_ORIGIN)
            return
        }

        request.setAttribute(ATTRIBUTE_NAME, origin)
        filterChain.doFilter(request, response)
    }

    private fun resolveOrigin(request: HttpServletRequest): String? =
        OriginArgumentUtil.resolveOrigin(
            request.getHeader(HttpHeaders.ORIGIN),
            request.scheme,
            request.serverName,
            request.serverPort,
        )
}
