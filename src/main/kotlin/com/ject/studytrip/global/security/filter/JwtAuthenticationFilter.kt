package com.ject.studytrip.global.security.filter

import com.ject.studytrip.auth.application.service.TokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val tokenService: TokenService,
) : OncePerRequestFilter() {
    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        extractBearerToken(request)
            ?.let { accessToken ->
                tokenService.validateActiveAccessToken(accessToken)
                tokenService.setAuthenticationByAccessToken(accessToken)
            }

        filterChain.doFilter(request, response)
    }

    private fun extractBearerToken(request: HttpServletRequest): String? {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (header.isNullOrBlank()) return null
        if (!header.startsWith(BEARER_PREFIX)) return null

        return header
            .removePrefix(BEARER_PREFIX)
            .trim()
            .takeIf { it.isNotBlank() }
    }
}
