package com.ject.studytrip.global.security

import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.global.security.handler.SecurityResponseHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint(
    private val securityResponseHandler: SecurityResponseHandler,
) : AuthenticationEntryPoint {
    companion object {
        private val log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint::class.java)
    }

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: AuthenticationException,
    ) {
        log.warn("AuthenticationException: uri={}, message={}", request.requestURI, e.message)

        securityResponseHandler.sendResponse(response, AuthErrorCode.UNAUTHENTICATED)
    }
}
