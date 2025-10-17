package com.ject.studytrip.trip.domain.factory;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.trip.domain.model.TripReport;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TripReportFactory {
    public static TripReport create(
            Member member,
            String title,
            String content,
            String startDate,
            String endDate,
            long studyLogCount,
            long totalFocusHours,
            long studyDays,
            String imageTitle) {
        return TripReport.of(
                member,
                title,
                content,
                startDate,
                endDate,
                studyLogCount,
                totalFocusHours,
                studyDays,
                imageTitle);
    }
}
