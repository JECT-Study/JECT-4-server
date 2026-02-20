package com.ject.studytrip.mission.domain.repository

interface MissionCommandRepository {
    fun existsByStampIdAndCompletedIsFalseAndDeletedAtIsNull(stampId: Long): Boolean

    fun deleteAllByDeletedAtIsNotNull(): Long

    fun deleteAllByDeletedStampOwner(): Long

    fun deleteAllByMemberId(memberId: Long): Long
}
