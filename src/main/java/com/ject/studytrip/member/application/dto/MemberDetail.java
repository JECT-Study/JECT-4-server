package com.ject.studytrip.member.application.dto;

import com.ject.studytrip.trip.application.dto.TripCount;

public record MemberDetail(MemberInfo memberInfo, TripCount tripCount, long studyLogCount) {
    public static MemberDetail from(
            MemberInfo memberInfo, TripCount tripCount, long studyLogCount) {
        return new MemberDetail(memberInfo, tripCount, studyLogCount);
    }
}
