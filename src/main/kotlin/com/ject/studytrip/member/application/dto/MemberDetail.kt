package com.ject.studytrip.member.application.dto

import com.ject.studytrip.trip.application.dto.TripCount

data class MemberDetail(
    val memberInfo: MemberInfo,
    val tripCount: TripCount,
    val studyLogCount: Long,
)
