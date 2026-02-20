package com.ject.studytrip.global.exception.handler

import com.ject.studytrip.global.common.response.StandardResponse
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.global.exception.error.CommonErrorCode
import com.ject.studytrip.global.exception.error.ErrorCode
import com.ject.studytrip.global.exception.response.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    /**
     * CustomException 예외 처리
     */
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<StandardResponse> {
        log.warn("CustomException: {}", e.message)

        return buildResponse(e.errorCode)
    }

    /**
     * 500번대 에러 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<StandardResponse> {
        log.error("Internal Server Error: {}", e.message, e)

        return buildResponse(CommonErrorCode.INTERNAL_SERVER_ERROR)
    }

    private fun buildResponse(errorCode: ErrorCode): ResponseEntity<StandardResponse> {
        val errorResponse = ErrorResponse.of(errorCode.name, errorCode.message)
        val response = StandardResponse.fail(errorCode.status.value(), errorResponse)

        return ResponseEntity
            .status(errorCode.status)
            .body(response)
    }
}
