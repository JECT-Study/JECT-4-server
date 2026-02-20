package com.ject.studytrip.trip.domain.error

import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class TripErrorCode(
    override val status: HttpStatus,
    override val message: String,
) : ErrorCode {
    // 400
    TRIP_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 여행입니다."),
    TRIP_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 여행입니다."),
    TRIP_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "여행이 아직 완료되지 않았습니다."),
    COURSE_TRIP_END_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "코스형 여행은 종료일이 필수입니다."),
    INVALID_TRIP_CATEGORY(HttpStatus.BAD_REQUEST, "여행 카테고리가 누락되거나 올바르지 않습니다."),

    // 403
    NOT_TRIP_OWNER(HttpStatus.FORBIDDEN, "여행을 수정/삭제할 권한이 없습니다."),

    // 404
    TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "요창한 여행을 찾을 수 없습니다."),
}
