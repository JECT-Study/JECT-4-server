package com.ject.studytrip.global.exception.handler;

import com.ject.studytrip.global.common.response.StandardResponse;
import com.ject.studytrip.global.exception.error.CommonErrorCode;
import com.ject.studytrip.global.exception.error.ErrorCode;
import com.ject.studytrip.global.exception.response.ErrorResponse;
import com.ject.studytrip.global.exception.response.FieldErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ValidationExceptionHandler extends ResponseEntityExceptionHandler {

    /** ResponseEntityExceptionHandler 가 기본으로 처리하는 예외를 공통으로 처리 */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception e,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest webRequest) {
        ErrorResponse errorResponse =
                ErrorResponse.of(e.getClass().getSimpleName(), e.getMessage());

        return super.handleExceptionInternal(e, errorResponse, headers, statusCode, webRequest);
    }

    /** 요청 본문(Json) 에서 유효성 제약 조건 위반 시 발생(바인딩 실패), 주로 RequestBody, RequestPart 에서 발생 */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest webRequest) {
        log.error("MethodArgumentNotValidException : {}", e.getMessage(), e);

        List<FieldErrorResponse> fieldErrors =
                e.getBindingResult().getFieldErrors().stream()
                        .map(
                                fieldError ->
                                        FieldErrorResponse.of(
                                                fieldError.getField(),
                                                fieldError.getDefaultMessage()))
                        .toList();

        final ErrorCode errorCode = CommonErrorCode.METHOD_ARGUMENT_NOT_VALID;
        final ErrorResponse errorResponse =
                ErrorResponse.of(errorCode.getName(), errorCode.getMessage(), fieldErrors);
        final StandardResponse response = StandardResponse.fail(statusCode.value(), errorResponse);

        return ResponseEntity.status(statusCode).body(response);
    }

    /** 요청 본문(JSON) 형식이 잘못되어 파싱할 수 없는 경우 발생 (필드 타입 불일치, 필수 필드 누락 시 발생) */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        log.error("HttpMessageNotReadableException : {}", e.getMessage(), e);

        final ErrorCode errorCode = CommonErrorCode.INVALID_JSON_FORMAT;
        final ErrorResponse errorResponse =
                ErrorResponse.of(e.getClass().getSimpleName(), errorCode.getMessage());
        final StandardResponse response =
                StandardResponse.fail(errorCode.getStatus().value(), errorResponse);

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    /** RequestParam, PathVariable 등 메서드 파라미터에서 제약 조건을 위반해 바인딩 실패 시 발생 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<StandardResponse> handleConstrainViolationException(
            ConstraintViolationException e) {
        log.error("ConstrainViolationException : {}", e.getMessage(), e);

        List<FieldErrorResponse> bindingErrors =
                e.getConstraintViolations().stream()
                        .map(
                                constraintViolation -> {
                                    List<String> propertyPath =
                                            List.of(
                                                    constraintViolation
                                                            .getPropertyPath()
                                                            .toString()
                                                            .split("\\."));

                                    String path =
                                            propertyPath.stream()
                                                    .skip(propertyPath.size() - 1L)
                                                    .findFirst()
                                                    .orElse(null);

                                    return FieldErrorResponse.of(
                                            path, constraintViolation.getMessage());
                                })
                        .toList();

        final ErrorCode errorCode = CommonErrorCode.CONSTRAINT_VIOLATION;
        final ErrorResponse errorResponse =
                ErrorResponse.of(errorCode.getName(), errorCode.getMessage(), bindingErrors);
        final StandardResponse response =
                StandardResponse.fail(HttpStatus.BAD_REQUEST.value(), errorResponse);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /** PathVariable, RequestParam, RequestHeader 에서 요청한 메서드 파라미터 타입이 일치하지 않은 경우 발생 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<StandardResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException : {}", e.getMessage(), e);

        final ErrorCode errorCode = CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH;
        final ErrorResponse errorResponse =
                ErrorResponse.of(e.getClass().getSimpleName(), errorCode.getMessage());
        final StandardResponse response =
                StandardResponse.fail(errorCode.getStatus().value(), errorResponse);

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    /** 지원하지 않는 HTTP method 요청 시 발생 */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException e,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest webRequest) {
        log.error("HttpRequestMethodNotSupportedException : {}", e.getMethod(), e);

        final ErrorCode errorCode = CommonErrorCode.METHOD_NOT_ALLOWED;
        final ErrorResponse errorResponse =
                ErrorResponse.of(e.getClass().getSimpleName(), errorCode.getMessage());
        final StandardResponse response =
                StandardResponse.fail(errorCode.getStatus().value(), errorResponse);

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}
