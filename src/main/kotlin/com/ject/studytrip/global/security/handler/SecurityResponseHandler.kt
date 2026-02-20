package com.ject.studytrip.global.security.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.ject.studytrip.global.common.response.StandardResponse
import com.ject.studytrip.global.exception.error.ErrorCode
import com.ject.studytrip.global.exception.response.ErrorResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class SecurityResponseHandler(
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private const val CONTENT_TYPE = "application/json;charset=UTF-8"
    }

    fun sendResponse(
        response: HttpServletResponse,
        errorCode: ErrorCode,
    ) {
        val errorResponse = ErrorResponse.of(errorCode.name, errorCode.message)
        val standardResponse = StandardResponse.fail(errorCode.status.value(), errorResponse)
        val json = objectMapper.writeValueAsString(standardResponse)

        response.apply {
            status = errorCode.status.value()
            contentType = CONTENT_TYPE
            writer.write(json)
        }
    }
}
