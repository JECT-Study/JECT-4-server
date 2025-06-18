package com.ject.studytrip.global.exception.response;

public record FieldErrorResponse(String field, String reason) {

    public static FieldErrorResponse of(String field, String reason) {
        return new FieldErrorResponse(field, reason);
    }
}
