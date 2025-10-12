package com.ject.studytrip.trip.domain.error;

import com.ject.studytrip.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum TripReportErrorCode implements ErrorCode {
    // 403
    NOT_TRIP_REPORT_OWNER(HttpStatus.FORBIDDEN, "요청한 여행 리포트 정보를 조회할 권한이 없습니다."),

    // 404
    TRIP_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 여행 리포트가 존재하지 않습니다.");

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
