package com.ject.studytrip.trip.domain.repository

interface TripCommandRepository {
    fun deleteAllByDeletedAtIsNotNull(): Long

    fun deleteAllByDeletedMemberOwner(): Long

    fun deleteAllByMemberId(memberId: Long): Long
}
