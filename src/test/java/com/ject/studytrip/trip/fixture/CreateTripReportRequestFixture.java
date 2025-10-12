package com.ject.studytrip.trip.fixture;

import com.ject.studytrip.trip.presentation.dto.request.CreateTripReportRequest;
import java.util.List;

public class CreateTripReportRequestFixture {
    private static final String TRIP_REPORT_TITLE = "TEST TITLE";
    private static final String TRIP_REPORT_CONTENT = "TEST CONTENT";
    private static final String TRIP_START_DATE = "2018.01.01";
    private static final String TRIP_END_DATE = "2018.01.31";
    private static final long TRIP_REPORT_COMPLETED_MISSION_COUNT = 10L;
    private static final long TRIP_REPORT_TOTAL_FOCUS_HOURS = 100L;
    private static final long TRIP_REPORT_STUDY_DAYS = 10L;
    private static final String TRIP_REPORT_IMAGE_TITLE = "TEST IMAGE TITLE";

    private List<Long> studyLogIds = List.of(1L, 2L);

    public CreateTripReportRequestFixture withStudyLogIds(List<Long> studyLogIds) {
        this.studyLogIds = studyLogIds;
        return this;
    }

    public CreateTripReportRequest build() {
        return new CreateTripReportRequest(
                TRIP_REPORT_TITLE,
                TRIP_REPORT_CONTENT,
                TRIP_START_DATE,
                TRIP_END_DATE,
                TRIP_REPORT_COMPLETED_MISSION_COUNT,
                TRIP_REPORT_TOTAL_FOCUS_HOURS,
                TRIP_REPORT_STUDY_DAYS,
                TRIP_REPORT_IMAGE_TITLE,
                studyLogIds);
    }
}
