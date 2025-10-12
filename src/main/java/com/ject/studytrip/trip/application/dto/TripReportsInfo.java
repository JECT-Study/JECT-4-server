package com.ject.studytrip.trip.application.dto;

import java.util.List;

public record TripReportsInfo(List<TripReportInfo> tripReportInfos) {
    public static TripReportsInfo of(List<TripReportInfo> tripReportInfos) {
        return new TripReportsInfo(tripReportInfos);
    }
}
