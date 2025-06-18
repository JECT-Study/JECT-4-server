package com.ject.studytrip.global.exception.response;

public record ErrorResponse(String error, String message, Object values) {

    public static ErrorResponse of(String error, String message, Object values) {
        return new ErrorResponse(error, message, values);
    }

    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message, null);
    }
}
