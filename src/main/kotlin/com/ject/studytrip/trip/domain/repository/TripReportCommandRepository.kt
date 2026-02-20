package com.ject.studytrip.trip.domain.repository

interface TripReportCommandRepository {
    fun deleteAllByDeletedAtIsNotNull(): Long

    fun deleteAllByDeletedMemberOwner(): Long

    fun deleteAllByMemberId(memberId: Long): Long
}
