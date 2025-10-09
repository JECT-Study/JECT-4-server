package com.ject.studytrip.studylog.domain.error;

import com.ject.studytrip.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum StudyLogErrorCode implements ErrorCode {
    // 400
    STUDY_LOG_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 학습 로그입니다."),

    // 404
    STUDY_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "학습 로그를 찾을 수 없습니다."),
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
