package com.ject.studytrip.trip.domain.error

import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class DailyGoalErrorCode(
    private val status: HttpStatus,
    private val message: String,
) : ErrorCode {
    // 400
    DAILY_GOAL_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 데일리 목표입니다."),

    // 403
    DAILY_GOAL_NOT_BELONGS_TO_TRIP(HttpStatus.FORBIDDEN, "해당 데일리 목표는 요청한 여행에 속하지 않습니다."),

    // 404
    DAILY_GOAL_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 데일리 목표를 찾을 수 없습니다."),
    ;

    override fun getName(): String = name

    override fun getStatus(): HttpStatus = status

    override fun getMessage(): String = message
}
