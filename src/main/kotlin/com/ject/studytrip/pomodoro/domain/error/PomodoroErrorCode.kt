package com.ject.studytrip.pomodoro.domain.error

import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class PomodoroErrorCode(
    override val status: HttpStatus,
    override val message: String,
) : ErrorCode {
    // 400
    POMODORO_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 뽀모도로입니다."),
    POMODORO_NEGATIVE_FOCUS_TIME(HttpStatus.BAD_REQUEST, "뽀모도로 총 집중시간(분)은 음수일 수 없습니다."),

    // 404
    POMODORO_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 뽀모도로를 찾을 수 없습니다."),
}
