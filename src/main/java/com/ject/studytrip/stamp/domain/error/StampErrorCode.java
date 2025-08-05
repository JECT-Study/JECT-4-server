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
    STAMP_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 스탬프입니다."),
    CANNOT_UPDATE_ORDER_FOR_EXPLORATION_TRIP(HttpStatus.BAD_REQUEST, "탐험형 여행의 스탬프 순서는 변경할 수 없습니다."),
    INVALID_STAMP_ID_IN_REQUEST(HttpStatus.BAD_REQUEST, "존재하지 않는 스탬프 ID가 포함되어 있습니다. "),
    STAMP_LIST_CANNOT_BE_EMPTY(HttpStatus.BAD_REQUEST, "스탬프 목록은 비어있을 수 없습니다."),

    // 403
    STAMP_NOT_BELONG_TO_TRIP(HttpStatus.FORBIDDEN, "해당 스탬프는 요청한 여행에 속하지 않습니다."),

    // 404
    STAMP_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 스탬프가 존재하지 않습니다."),
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
