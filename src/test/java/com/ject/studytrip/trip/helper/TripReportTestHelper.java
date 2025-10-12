package com.ject.studytrip.trip.helper;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.trip.domain.model.TripReport;
import com.ject.studytrip.trip.domain.repository.TripReportRepository;
import com.ject.studytrip.trip.fixture.TripReportFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripReportTestHelper {

    @Autowired private TripReportRepository tripReportRepository;

    public TripReport saveTripReport(Member member) {
        TripReport tripReport = TripReportFixture.createTripReport(member);
        return tripReportRepository.save(tripReport);
    }
}
