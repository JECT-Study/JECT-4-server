package com.ject.studytrip.trip.domain.error;

import com.ject.studytrip.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum TripErrorCode implements ErrorCode {
    // 400
    TRIP_CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "여행 카테고리는 필수입니다."),
    INVALID_TRIP_CATEGORY(HttpStatus.BAD_REQUEST, "여행 카테고리가 누락되었거나 올바르지 않습니다."),
    TRIP_STAMP_REQUIRED(HttpStatus.BAD_REQUEST, "여행을 생성하려면 최소 1개의 스탬프가 필요합니다."),
    TRIP_END_DATE_BEFORE_START_DATE(HttpStatus.BAD_REQUEST, "여행 종료일은 시작일보다 이후여야 합니다."),
    COURSE_TRIP_END_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "코스형 여행은 종료일이 필수입니다."),
    TRIP_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 여행입니다."),
    TRIP_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 여행입니다."),
    TRIP_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "여행이 아직 완료되지 않았습니다."),

    // 403
    NOT_TRIP_OWNER(HttpStatus.FORBIDDEN, "요청한 여행 정보를 수정/삭제할 권한이 부족합니다."),

    // 404
    TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 여행이 존재하지 않습니다."),
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
