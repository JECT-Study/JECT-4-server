package com.ject.studytrip.global.security;

import com.ject.studytrip.auth.application.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String accessToken = extractBearerToken(authorizationHeader);

        if (StringUtils.hasText(accessToken)) {
            tokenService.validateActiveAccessToken(accessToken);
            tokenService.setAuthenticationByAccessToken(accessToken);
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(String header) {
        return (StringUtils.hasText(header) && header.startsWith("Bearer "))
                ? header.substring(7)
                : null;
    }
}
