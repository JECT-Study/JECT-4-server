package com.ject.studytrip.global.common.response;

import com.ject.studytrip.global.exception.response.ErrorResponse;

public record StandardResponse(boolean success, int status, Object data) {

    public static StandardResponse success(int status, Object data) {
        return new StandardResponse(true, status, data);
    }

    public static StandardResponse fail(int status, ErrorResponse errorResponse) {
        return new StandardResponse(false, status, errorResponse);
    }
}
