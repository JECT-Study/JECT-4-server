package com.ject.studytrip.mission.domain.error

import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class MissionErrorCode(
    private val status: HttpStatus,
    private val message: String,
) : ErrorCode {
    // 400
    MISSION_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 미션입니다."),
    MISSION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 미션입니다."),
    ALL_MISSIONS_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "모든 미션이 완료되지 않았습니다."),

    // 403
    MISSION_NOT_BELONGS_TO_STAMP(HttpStatus.FORBIDDEN, "해당 미션은 요청한 스탬프에 속하지 않습니다."),

    // 404
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 미션을 찾을 수 없습니다."),
    ;

    override fun getName(): String = name

    override fun getStatus(): HttpStatus = status

    override fun getMessage(): String = message
}
