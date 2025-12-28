package com.ject.studytrip.stamp.domain.error

import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class StampErrorCode(
    private val status: HttpStatus,
    private val message: String,
) : ErrorCode {
    // 400
    STAMP_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 스탬프입니다."),
    STAMP_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 스탬프입니다."),
    ALL_STAMPS_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "모든 스탬프가 완료되지 않았습니다."),
    STAMP_LIST_NOT_EMPTY(HttpStatus.BAD_REQUEST, "스탬프 목록은 비어있을 수 없습니다."),
    STAMP_END_DATE_AFTER_TRIP_END_DATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "스탬프 종료일은 여행 종료일보다 이후일 수 없습니다."),
    CANNOT_UPDATE_ORDER_FOR_EXPLORATION_TRIP(HttpStatus.BAD_REQUEST, "탐험형 여행의 스탬프 순서는 변경할 수 없습니다."),
    INVALID_STAMP_ID_IN_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 스탬프 ID가 요청에 포함되어 있습니다."),

    // 403
    STAMP_NOT_BELONGS_TO_TRIP(HttpStatus.FORBIDDEN, "해당 스탬프는 요청한 여행에 속하지 않습니다."),

    // 404
    STAMP_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 스탬프를 찾을 수 없습니다."),
    ;

    override fun getName(): String = name

    override fun getStatus(): HttpStatus = status

    override fun getMessage(): String = message
}
