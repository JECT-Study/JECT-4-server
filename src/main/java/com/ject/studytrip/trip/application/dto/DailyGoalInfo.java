package com.ject.studytrip.trip.application.dto;

import com.ject.studytrip.global.util.DateUtil;
import com.ject.studytrip.trip.domain.model.DailyGoal;

public record DailyGoalInfo(
        Long dailyGoalId, boolean completed, String createdAt, String updatedAt, String deletedAt) {
    public static DailyGoalInfo from(DailyGoal dailyGoal) {
        return new DailyGoalInfo(
                dailyGoal.getId(),
                dailyGoal.isCompleted(),
                DateUtil.formatDateTime(dailyGoal.getCreatedAt()),
                DateUtil.formatDateTime(dailyGoal.getUpdatedAt()),
                DateUtil.formatDateTime(dailyGoal.getDeletedAt()));
    }
}
