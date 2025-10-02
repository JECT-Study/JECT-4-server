package com.ject.studytrip.image.infra.tika.error;

import com.ject.studytrip.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum TikaErrorCode implements ErrorCode {
    TIKA_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Tika 서버 에러가 발생했습니다."),
    ;

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
