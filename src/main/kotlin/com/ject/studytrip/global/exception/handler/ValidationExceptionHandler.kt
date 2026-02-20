package com.ject.studytrip.global.exception.handler

import com.ject.studytrip.global.common.response.StandardResponse
import com.ject.studytrip.global.exception.error.CommonErrorCode
import com.ject.studytrip.global.exception.error.ErrorCode
import com.ject.studytrip.global.exception.response.ErrorResponse
import com.ject.studytrip.global.exception.response.FieldErrorResponse
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ValidationExceptionHandler : ResponseEntityExceptionHandler() {
    companion object {
        private val log = LoggerFactory.getLogger(ValidationExceptionHandler::class.java)
    }

    /**
     * ResponseEntityExceptionHandler 가 기본으로 처리하는 예외를 공통으로 처리
     */
    override fun handleExceptionInternal(
        e: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val errorResponse = ErrorResponse.of(e::class.simpleName ?: "Exception", e.message ?: "")

        return super.handleExceptionInternal(
            e,
            errorResponse,
            headers,
            statusCode,
            request,
        ) ?: ResponseEntity.status(statusCode).body(errorResponse)
    }

    /**
     * 요청 본문(Json) 에서 유효성 제약 조건 위반 시 발생(바인딩 실패), 주로 RequestBody, RequestPart 에서 발생
     */
    override fun handleMethodArgumentNotValid(
        e: MethodArgumentNotValidException,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        log.warn("MethodArgumentNotValidException: {}", e.message)

        val fieldErrors =
            e.bindingResult.fieldErrors.map {
                FieldErrorResponse.of(
                    it.field,
                    it.defaultMessage ?: "Invalid value",
                )
            }

        return buildValidationResponse(
            CommonErrorCode.METHOD_ARGUMENT_NOT_VALID,
            statusCode.value(),
            fieldErrors,
        )
    }

    /**
     * 요청 본문(JSON) 형식이 잘못되어 파싱할 수 없는 경우 발생 (필드 타입 불일치, 필수 필드 누락 시 발생)
     */
    override fun handleHttpMessageNotReadable(
        e: HttpMessageNotReadableException,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        log.warn("HttpMessageNotReadableException: {}", e.message)

        return buildErrorResponse(CommonErrorCode.INVALID_JSON_FORMAT)
    }

    /**
     * 지원하지 않는 HTTP method 요청 시 발생
     */
    override fun handleHttpRequestMethodNotSupported(
        e: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        log.warn("HttpRequestMethodNotSupportedException: {}", e.method)

        return buildErrorResponse(CommonErrorCode.METHOD_NOT_ALLOWED)
    }

    /**
     * RequestParam, PathVariable 등 메서드 파라미터에서 제약 조건을 위반해 바인딩 실패 시 발생
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(e: ConstraintViolationException): ResponseEntity<Any> {
        log.warn("ConstraintViolationException: {}", e.message)

        val fieldErrors =
            e.constraintViolations.map { violation ->
                val field =
                    violation.propertyPath
                        .toString()
                        .substringAfterLast(".")

                FieldErrorResponse.of(field, violation.message)
            }

        return buildValidationResponse(
            CommonErrorCode.CONSTRAINT_VIOLATION,
            HttpStatus.BAD_REQUEST.value(),
            fieldErrors,
        )
    }

    /**
     * PathVariable, RequestParam, RequestHeader 에서 요청한 메서드 파라미터 타입이 일치하지 않은 경우 발생
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<Any> {
        log.warn("MethodArgumentTypeMismatchException: {}", e.message)

        return buildErrorResponse(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH)
    }

    private fun buildErrorResponse(errorCode: ErrorCode): ResponseEntity<Any> {
        val errorResponse = ErrorResponse.of(errorCode.name, errorCode.message)
        val response = StandardResponse.fail(errorCode.status.value(), errorResponse)

        return ResponseEntity
            .status(errorCode.status)
            .body(response)
    }

    private fun buildValidationResponse(
        errorCode: ErrorCode,
        status: Int,
        fieldErrors: List<FieldErrorResponse>,
    ): ResponseEntity<Any> {
        val errorResponse = ErrorResponse.of(errorCode.name, errorCode.message, fieldErrors)
        val response = StandardResponse.fail(status, errorResponse)

        return ResponseEntity
            .status(status)
            .body(response)
    }
}
