package com.ject.studytrip.studylog.domain.factory;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StudyLogFactory {
    public static StudyLog create(
            Member member, DailyGoal dailyGoal, String title, String content) {
        return StudyLog.of(member, dailyGoal, title, content);
    }
}
