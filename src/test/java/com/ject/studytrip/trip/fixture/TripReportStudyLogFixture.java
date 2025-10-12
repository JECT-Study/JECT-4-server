package com.ject.studytrip.trip.fixture;

import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.trip.domain.factory.TripReportStudyLogFactory;
import com.ject.studytrip.trip.domain.model.TripReport;
import com.ject.studytrip.trip.domain.model.TripReportStudyLog;
import org.springframework.test.util.ReflectionTestUtils;

public class TripReportStudyLogFixture {

    public static TripReportStudyLog createTripReportStudyLog(
            TripReport tripReport, StudyLog studyLog) {
        return TripReportStudyLogFactory.create(tripReport, studyLog);
    }

    public static TripReportStudyLog createTripReportStudyLogWithId(
            Long id, TripReport tripReport, StudyLog studyLog) {
        TripReportStudyLog tripReportStudyLog =
                TripReportStudyLogFactory.create(tripReport, studyLog);
        ReflectionTestUtils.setField(tripReportStudyLog, "id", id);

        return tripReportStudyLog;
    }
}
