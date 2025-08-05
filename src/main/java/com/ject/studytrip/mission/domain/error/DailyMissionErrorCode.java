package com.ject.studytrip.mission.domain.error;

import com.ject.studytrip.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum DailyMissionErrorCode implements ErrorCode {
    // 400
    DAILY_MISSION_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 데일리 미션입니다."),
    COURSE_TRIP_STAMP_MISMATCH(HttpStatus.BAD_REQUEST, "코스형 여행의 데일리 미션들은 모두 동일한 스탬프여야 합니다."),

    // 403
    DAILY_MISSION_NOT_BELONG_TO_DAILY_GOAL(
            HttpStatus.FORBIDDEN, "해당 데일리 미션은 요청한 데일리 목표에 속하지 않습니다."),

    // 404
    DAILY_MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 데일리 미션이 존재하지 않습니다."),
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
