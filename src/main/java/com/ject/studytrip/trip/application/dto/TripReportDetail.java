package com.ject.studytrip.trip.application.dto;

import com.ject.studytrip.studylog.application.dto.StudyLogSliceInfo;

public record TripReportDetail(TripReportInfo tripReportInfo, StudyLogSliceInfo studyLogSliceInfo) {
    public static TripReportDetail from(
            TripReportInfo tripReportInfo, StudyLogSliceInfo studyLogSliceInfo) {
        return new TripReportDetail(tripReportInfo, studyLogSliceInfo);
    }
}
