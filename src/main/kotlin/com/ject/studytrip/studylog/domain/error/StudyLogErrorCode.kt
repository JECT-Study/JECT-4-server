package com.ject.studytrip.studylog.domain.error

import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class StudyLogErrorCode(
    private val status: HttpStatus,
    private val message: String,
) : ErrorCode {
    // 400
    STUDY_LOG_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 학습 로그입니다."),

    // 404
    STUDY_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 학습 로그를 찾을 수 없습니다."),
    ;

    override fun getName(): String = name

    override fun getStatus(): HttpStatus = status

    override fun getMessage(): String = message
}
