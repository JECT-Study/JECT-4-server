package com.ject.studytrip.image.infra.s3.error;

import com.ject.studytrip.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum S3ErrorCode implements ErrorCode {
    S3_STORAGE_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "Storage 서버 에러가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
