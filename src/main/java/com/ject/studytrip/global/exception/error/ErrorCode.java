package com.ject.studytrip.global.exception.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    String getName();

    HttpStatus getStatus();

    String getMessage();
}
