package com.ject.studytrip.global.security;

import static com.ject.studytrip.global.common.constants.UrlConstants.*;

import com.google.common.net.HttpHeaders;
import com.ject.studytrip.global.exception.error.CommonErrorCode;
import com.ject.studytrip.global.util.OriginArgumentUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class OriginExtractionFilter extends OncePerRequestFilter {
    private static final String ATTRIBUTE_NAME = "origin";

    private final SecurityResponseHandler securityResponseHandler;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String origin =
                OriginArgumentUtil.resolveOrigin(
                        request.getHeader(HttpHeaders.ORIGIN),
                        request.getScheme(),
                        request.getServerName(),
                        request.getServerPort());

        // origin null 검증
        if (origin == null) {
            securityResponseHandler.sendResponse(response, CommonErrorCode.INVALID_ORIGIN);
            return;
        }

        // 허용된 origin 검증
        List<String> allowedOrigins = Arrays.asList(CORS_DOMAINS.getUrls());
        if (!allowedOrigins.contains(origin)) {
            securityResponseHandler.sendResponse(response, CommonErrorCode.UNSUPPORTED_ORIGIN);
            return;
        }

        request.setAttribute(ATTRIBUTE_NAME, origin);
        filterChain.doFilter(request, response);
    }
}
