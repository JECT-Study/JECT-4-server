package com.ject.studytrip.trip.domain.repository

interface TripReportStudyLogCommandRepository {
    fun deleteAllByDeletedMemberOwner(): Long

    fun deleteAllByMemberId(memberId: Long): Long
}
