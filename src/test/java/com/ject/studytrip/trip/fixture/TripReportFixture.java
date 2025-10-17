package com.ject.studytrip.trip.fixture;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.trip.domain.factory.TripReportFactory;
import com.ject.studytrip.trip.domain.model.TripReport;
import org.springframework.test.util.ReflectionTestUtils;

public class TripReportFixture {
    private static final String TRIP_REPORT_TITLE = "TEST TITLE";
    private static final String TRIP_REPORT_CONTENT = "TEST CONTENT";
    private static final String TRIP_START_DATE = "2018.01.01";
    private static final String TRIP_END_DATE = "2018.01.31";
    private static final long STUDY_LOG_COUNT = 10L;
    private static final long TRIP_REPORT_TOTAL_FOCUS_HOURS = 100L;
    private static final long TRIP_REPORT_STUDY_DAYS = 10L;
    private static final String TRIP_REPORT_IMAGE_TITLE = "TEST IMAGE TITLE";

    public static TripReport createTripReport(Member member) {
        return TripReportFactory.create(
                member,
                TRIP_REPORT_TITLE,
                TRIP_REPORT_CONTENT,
                TRIP_START_DATE,
                TRIP_END_DATE,
                STUDY_LOG_COUNT,
                TRIP_REPORT_TOTAL_FOCUS_HOURS,
                TRIP_REPORT_STUDY_DAYS,
                TRIP_REPORT_IMAGE_TITLE);
    }

    public static TripReport createTripReportWithId(Long id, Member member) {
        TripReport tripReport =
                TripReportFactory.create(
                        member,
                        TRIP_REPORT_TITLE,
                        TRIP_REPORT_CONTENT,
                        TRIP_START_DATE,
                        TRIP_END_DATE,
                        STUDY_LOG_COUNT,
                        TRIP_REPORT_TOTAL_FOCUS_HOURS,
                        TRIP_REPORT_STUDY_DAYS,
                        TRIP_REPORT_IMAGE_TITLE);
        ReflectionTestUtils.setField(tripReport, "id", id);

        return tripReport;
    }
}
