package com.ject.studytrip.trip.domain.repository

interface DailyGoalCommandRepository {
    fun deleteAllByDeletedAtIsNotNull(): Long

    fun deleteAllByDeletedTripOwner(): Long

    fun deleteAllByMemberId(memberId: Long): Long
}
