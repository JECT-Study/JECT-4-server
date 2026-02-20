package com.ject.studytrip.trip.application.dto

data class TripRetrospectSummary(
    val studyLogCount: Long, // 학습 로그 개수
    val totalFocusHours: Long, // 총 집중 시간 (시간 단위)
    val studyDays: Long, // 학습한 일수 (중복 날짜 제거)
    val studyLogIds: List<Long>, // 학습 로그 ID 목록
)
