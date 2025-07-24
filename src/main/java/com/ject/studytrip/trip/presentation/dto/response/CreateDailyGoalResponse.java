package com.ject.studytrip.trip.presentation.dto.response;

import com.ject.studytrip.trip.application.dto.DailyGoalInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateDailyGoalResponse(@Schema(name = "데일리 목표 ID") Long dailyGoalId) {
    public static CreateDailyGoalResponse of(DailyGoalInfo dailyGoalInfo) {
        return new CreateDailyGoalResponse(dailyGoalInfo.dailyGoalId());
    }
}
