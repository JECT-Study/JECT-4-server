package com.ject.studytrip.pomodoro.domain.repository

interface PomodoroCommandRepository {
    fun deleteAllByDeletedAtIsNotNull(): Long

    fun deleteAllByDeletedDailyGoalOwner(): Long

    fun deleteAllByMemberId(memberId: Long): Long
}
