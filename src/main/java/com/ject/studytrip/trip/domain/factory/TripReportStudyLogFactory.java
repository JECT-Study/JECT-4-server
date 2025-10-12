package com.ject.studytrip.trip.domain.factory;

import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.trip.domain.model.TripReport;
import com.ject.studytrip.trip.domain.model.TripReportStudyLog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TripReportStudyLogFactory {
    public static TripReportStudyLog create(TripReport tripReport, StudyLog studyLog) {
        return TripReportStudyLog.of(tripReport, studyLog);
    }
}
