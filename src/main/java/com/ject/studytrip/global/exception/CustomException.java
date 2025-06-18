package com.ject.studytrip.global.exception;

import com.ject.studytrip.global.exception.error.CommonErrorCode;
import com.ject.studytrip.global.exception.error.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(CommonErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
