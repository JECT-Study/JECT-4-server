package com.ject.studytrip.trip.application.dto

import com.ject.studytrip.global.util.DateUtil
import com.ject.studytrip.trip.domain.model.DailyGoal

data class DailyGoalInfo(
    val dailyGoalId: Long,
    val title: String,
    val completed: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
) {
    companion object {
        @JvmStatic
        fun from(dailyGoal: DailyGoal): DailyGoalInfo =
            DailyGoalInfo(
                dailyGoal.id,
                dailyGoal.title,
                dailyGoal.isCompleted,
                DateUtil.formatDateTime(dailyGoal.createdAt),
                DateUtil.formatDateTime(dailyGoal.updatedAt),
                dailyGoal.deletedAt?.let { DateUtil.formatDateTime(it) },
            )
    }
}
