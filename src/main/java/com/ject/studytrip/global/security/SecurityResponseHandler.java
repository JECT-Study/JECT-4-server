package com.ject.studytrip.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ject.studytrip.global.common.response.StandardResponse;
import com.ject.studytrip.global.exception.error.ErrorCode;
import com.ject.studytrip.global.exception.response.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityResponseHandler {

    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

    private final ObjectMapper objectMapper;

    public void sendResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        final ErrorResponse errorResponse =
                ErrorResponse.of(errorCode.getName(), errorCode.getMessage());
        final StandardResponse standardResponse =
                StandardResponse.fail(errorCode.getStatus().value(), errorResponse);
        final String json = objectMapper.writeValueAsString(standardResponse);

        response.setStatus(standardResponse.status());
        response.setContentType(CONTENT_TYPE);
        response.getWriter().write(json);
    }
}
