package com.ject.studytrip.mission.domain.error

import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class DailyMissionErrorCode(
    override val status: HttpStatus,
    override val message: String,
) : ErrorCode {
    // 400
    DAILY_MISSION_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 데일리 미션입니다."),

    // 403
    DAILY_MISSION_NOT_BELONGS_TO_DAILY_GOAL(HttpStatus.FORBIDDEN, "해당 데일리 미션은 요청한 데일리 목표에 속하지 않습니다."),

    // 404
    DAILY_MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 데일리 미션을 찾을 수 없습니다."),
}
