package com.ject.studytrip.mission.domain.error;

import com.ject.studytrip.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum MissionErrorCode implements ErrorCode {
    // 400
    MISSION_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "해당 미션은 이미 삭제되었습니다."),
    MISSION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 미션입니다."),
    ALL_MISSIONS_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "모든 미션이 완료되지 않았습니다."),

    // 403
    MISSION_NOT_BELONGS_TO_STAMP(HttpStatus.FORBIDDEN, "해당 미션은 요청한 스탬프에 속하지 않습니다."),

    // 404
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 미션이 존재하지 않습니다."),
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
