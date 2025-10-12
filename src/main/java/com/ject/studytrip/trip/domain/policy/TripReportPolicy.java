package com.ject.studytrip.trip.domain.policy;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.trip.domain.error.TripReportErrorCode;
import com.ject.studytrip.trip.domain.model.TripReport;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TripReportPolicy {
    public static void validateOwner(Long memberId, TripReport tripReport) {
        if (!tripReport.getMember().getId().equals(memberId)) {
            throw new CustomException(TripReportErrorCode.NOT_TRIP_REPORT_OWNER);
        }
    }
}
