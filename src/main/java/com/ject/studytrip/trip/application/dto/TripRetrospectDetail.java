package com.ject.studytrip.trip.application.dto;

import com.ject.studytrip.studylog.application.dto.StudyLogSliceInfo;

public record TripRetrospectDetail(
        TripRetrospectSummary summary, TripInfo tripInfo, StudyLogSliceInfo studyLogDetailSlice) {
    public static TripRetrospectDetail from(
            TripRetrospectSummary summary,
            TripInfo tripInfo,
            StudyLogSliceInfo studyLogDetailSlice) {
        return new TripRetrospectDetail(summary, tripInfo, studyLogDetailSlice);
    }
}
