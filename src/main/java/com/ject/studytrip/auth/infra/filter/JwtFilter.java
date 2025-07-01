package com.ject.studytrip.auth.infra.filter;

import com.ject.studytrip.auth.infra.provider.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = extractToken(authorizationHeader);
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            setAuthentication(token);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(String header) {
        return (StringUtils.hasText(header) && header.startsWith("Bearer "))
                ? header.substring(7)
                : null;
    }

    private void setAuthentication(String token) {
        String memberId = tokenProvider.extractMemberIdFromToken(token);
        String memberRole = tokenProvider.extractMemberRoleFromToken(token);
        UsernamePasswordAuthenticationToken authentication =
                getAuthentication(memberId, memberRole);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(
            String memberId, String memberRole) {
        var authorities = List.of(new SimpleGrantedAuthority(memberRole));
        return new UsernamePasswordAuthenticationToken(memberId, null, authorities);
    }
}
