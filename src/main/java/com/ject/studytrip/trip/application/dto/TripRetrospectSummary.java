package com.ject.studytrip.trip.application.dto;

public record TripRetrospectSummary(
        long studyLogCount, // 학습 로그 개수
        long totalFocusHours, // 총 집중 시간(시간 단위)
        long studyDays // 학습한 일수(중복 날짜 제거)
        ) {
    public static TripRetrospectSummary of(
            long studyLogCount, long totalFocusHours, long studyDays) {
        return new TripRetrospectSummary(studyLogCount, totalFocusHours, studyDays);
    }
}
