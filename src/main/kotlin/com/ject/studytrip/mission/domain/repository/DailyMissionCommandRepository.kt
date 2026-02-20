package com.ject.studytrip.mission.domain.repository

interface DailyMissionCommandRepository {
    fun deleteAllByDeletedAtIsNotNull(): Long

    fun deleteAllByDeletedMissionOwner(): Long

    fun deleteAllByDeletedDailyGoalOwner(): Long

    fun deleteAllByMemberId(memberId: Long): Long
}
