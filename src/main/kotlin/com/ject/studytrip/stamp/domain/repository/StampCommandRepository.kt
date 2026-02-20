package com.ject.studytrip.stamp.domain.repository

interface StampCommandRepository {
    fun existsByTripIdAndCompletedIsFalseAndDeletedAtIsNull(tripId: Long): Boolean

    fun deleteAllByDeletedAtIsNotNull(): Long

    fun deleteAllByDeletedTripOwner(): Long

    fun deleteAllByMemberId(memberId: Long): Long
}
