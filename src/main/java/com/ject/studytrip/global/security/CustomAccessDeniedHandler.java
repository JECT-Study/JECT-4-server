package com.ject.studytrip.global.security;

import com.ject.studytrip.global.exception.error.AuthErrorCode;
import com.ject.studytrip.global.exception.error.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityResponseHandler securityResponseHandler;

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException, ServletException {
        log.error("AccessDeniedException : {}", e.getMessage(), e);

        final ErrorCode errorCode = AuthErrorCode.ACCESS_DENIED;
        securityResponseHandler.sendResponse(response, errorCode);
    }
}
