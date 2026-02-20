package com.ject.studytrip.studylog.domain.repository

interface StudyLogCommandRepository {
    fun deleteAllByDeletedAtIsNotNull(): Long

    fun deleteAllByDeletedMemberOwner(): Long

    fun deleteAllByDeletedDailyGoalOwner(): Long

    fun deleteByMemberId(memberId: Long): Long
}
