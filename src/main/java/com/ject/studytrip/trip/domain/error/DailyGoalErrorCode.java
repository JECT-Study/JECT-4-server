package com.ject.studytrip.trip.domain.error;

import com.ject.studytrip.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum DailyGoalErrorCode implements ErrorCode {
    // 400
    DAILY_GOAL_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 데일리 목표입니다."),

    // 403
    DAILY_GOAL_NOT_BELONG_TO_TRIP(HttpStatus.FORBIDDEN, "해당 데일리 목표는 요청한 여행에 속하지 않습니다."),

    //  404
    DAILY_GOAL_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 데일리 목표가 존재하지 않습니다."),
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
