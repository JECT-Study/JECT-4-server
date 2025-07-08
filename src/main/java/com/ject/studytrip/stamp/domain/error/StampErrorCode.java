package com.ject.studytrip.stamp.domain.error;

import com.ject.studytrip.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum StampErrorCode implements ErrorCode {
    // 400
    STAMP_DEADLINE_CANNOT_BE_IN_PAST(HttpStatus.BAD_REQUEST, "스탬프의 마감일은 과거일 수 없습니다."),
    STAMP_DEADLINE_EXCEEDS_TRIP_END_DATE(HttpStatus.BAD_REQUEST, "스탬프의 마감일은 여행 종료일을 초과할 수 없습니다."),
    INVALID_STAMP_ORDER_FOR_EXPLORATION_TRIP(
            HttpStatus.BAD_REQUEST, "탐험형 여행에서는 스탬프 순서를 지정할 수 없으며, 항상 0이여야 합니다. "),
    INVALID_STAMP_ORDER_RANGE_FOR_COURSE_TRIP(
            HttpStatus.BAD_REQUEST, "코스형 여행의 스탬프 순서의 범위는 최소 1 이상 또는 최대 총 스탬프 개수여야 합니다."),
    DUPLICATE_STAMP_ORDER_FOR_COURSE_TRIP(HttpStatus.BAD_REQUEST, "코스형 여행의 스탬프 순서에 중복된 값이 존재합니다."),
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
