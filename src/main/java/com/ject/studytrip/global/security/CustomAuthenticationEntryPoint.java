package com.ject.studytrip.global.security;

import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.global.exception.error.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityResponseHandler securityResponseHandler;

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException, ServletException {
        log.error("AuthenticationException : {}", e.getMessage(), e);

        final ErrorCode errorCode = AuthErrorCode.UNAUTHENTICATED;
        securityResponseHandler.sendResponse(response, errorCode);
    }
}
