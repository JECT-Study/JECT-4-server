package com.ject.studytrip.member.domain.error

import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class MemberErrorCode(
    private val status: HttpStatus,
    private val message: String,
) : ErrorCode {
    // 400
    MEMBER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 멤버입니다."),
    MEMBER_NOT_DELETED(HttpStatus.BAD_REQUEST, "멤버가 아직 삭제되지 않았습니다."),
    INVALID_MEMBER_CATEGORY(HttpStatus.BAD_REQUEST, "멤버 카테고리가 누락되거나 올바르지 않습니다."),

    // 404
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "요창한 멤버를 찾을 수 없습니다."),

    // 409
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 회원가입된 멤버입니다."),
    ;

    override fun getName(): String = name

    override fun getStatus(): HttpStatus = status

    override fun getMessage(): String = message
}
