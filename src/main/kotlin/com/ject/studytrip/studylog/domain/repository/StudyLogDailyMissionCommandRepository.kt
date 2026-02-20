package com.ject.studytrip.studylog.domain.repository

interface StudyLogDailyMissionCommandRepository {
    fun deleteAllByDeletedAtIsNotNull(): Long

    fun deleteAllByDeletedDailyMissionOwner(): Long

    fun deleteAllByDeletedStudyLogOwner(): Long

    fun deleteAllByMemberId(memberId: Long): Long
}
