package com.ject.studytrip.member.application.dto;

import com.ject.studytrip.trip.application.dto.TripCountInfo;

public record MemberDetail(MemberInfo memberInfo, TripCountInfo tripCount, long studyLogCount) {
    public static MemberDetail from(
            MemberInfo memberInfo, TripCountInfo tripCount, long studyLogCount) {
        return new MemberDetail(memberInfo, tripCount, studyLogCount);
    }
}
