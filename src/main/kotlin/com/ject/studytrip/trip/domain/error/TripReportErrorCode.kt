package com.ject.studytrip.trip.domain.error

import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class TripReportErrorCode(
    private val status: HttpStatus,
    private val message: String,
) : ErrorCode {
    // 400
    TRIP_REPORT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 여행 리포트입니다."),

    // 403
    NOT_TRIP_REPORT_OWNER(HttpStatus.FORBIDDEN, "여행 리포트를 수정/삭제할 권한이 없습니다."),

    // 404
    TRIP_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "요창한 여행 리포트를 찾을 수 없습니다."),
    ;

    override fun getName(): String = name

    override fun getStatus(): HttpStatus = status

    override fun getMessage(): String = message
}
