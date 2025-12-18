package com.ject.studytrip.member.domain.error;

import com.ject.studytrip.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    // 400
    INVALID_MEMBER_CATEGORY(HttpStatus.BAD_REQUEST, "유효하지 않은 멤버 카테고리입니다."),
    MEMBER_NICKNAME_DUPLICATED(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),
    MEMBER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "해당 멤버는 이미 삭제되었습니다."),
    MEMBER_NOT_DELETED(HttpStatus.BAD_REQUEST, "해당 멤버는 삭제되지 않았습니다."),

    // 404
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."),

    // 409
    MEMBER_NEED_SIGNUP(HttpStatus.CONFLICT, "회원가입이 필요한 사용자입니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 사용자입니다."),
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
