package com.ject.studytrip.global.security.handler

import com.ject.studytrip.auth.domain.error.AuthErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler(
    private val securityResponseHandler: SecurityResponseHandler,
) : AccessDeniedHandler {
    companion object {
        private val log = LoggerFactory.getLogger(CustomAccessDeniedHandler::class.java)
    }

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: AccessDeniedException,
    ) {
        log.warn("AccessDeniedException: uri={}, message={}", request.requestURI, e.message)

        securityResponseHandler.sendResponse(response, AuthErrorCode.ACCESS_DENIED)
    }
}
