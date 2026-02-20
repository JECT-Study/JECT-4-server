package com.ject.studytrip.trip.application.dto

import com.ject.studytrip.global.util.DateUtil
import com.ject.studytrip.global.util.EntityExtensions.requireId
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
        fun from(dailyGoal: DailyGoal): DailyGoalInfo =
            DailyGoalInfo(
                dailyGoal.id.requireId(),
                dailyGoal.title,
                dailyGoal.isCompleted(),
                DateUtil.formatDateTime(requireNotNull(dailyGoal.createdAt)),
                DateUtil.formatDateTime(requireNotNull(dailyGoal.updatedAt)),
                dailyGoal.deletedAt?.let { DateUtil.formatDateTime(it) },
            )
    }
}
