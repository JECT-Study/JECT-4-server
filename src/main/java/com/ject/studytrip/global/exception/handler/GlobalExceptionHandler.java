package com.ject.studytrip.global.exception.handler;

import com.ject.studytrip.global.common.response.StandardResponse;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.global.exception.error.CommonErrorCode;
import com.ject.studytrip.global.exception.error.ErrorCode;
import com.ject.studytrip.global.exception.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /** CustomException 예외 처리 */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<StandardResponse> handleCustomException(CustomException e) {
        log.error("CustomException : {}", e.getMessage(), e);

        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse errorResponse =
                ErrorResponse.of(errorCode.getName(), errorCode.getMessage());
        final StandardResponse response =
                StandardResponse.fail(errorCode.getStatus().value(), errorResponse);

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    /** 500번대 에러 처리 */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<StandardResponse> handleException(Exception e) {
        log.error("Internal Server Error : {}", e.getMessage(), e);

        final ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        final ErrorResponse errorResponse =
                ErrorResponse.of(errorCode.getName(), errorCode.getMessage());
        final StandardResponse response =
                StandardResponse.fail(errorCode.getStatus().value(), errorResponse);

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}
