package com.ject.studytrip.member.application.dto

import com.ject.studytrip.trip.application.dto.TripCount

data class MemberDetail(
    val memberInfo: MemberInfo,
    val tripCount: TripCount,
    val studyLogCount: Long,
) {
    companion object {
        @JvmStatic
        fun from(
            memberInfo: MemberInfo,
            tripCount: TripCount,
            studyLogCount: Long,
        ): MemberDetail = MemberDetail(memberInfo, tripCount, studyLogCount)
    }
}
