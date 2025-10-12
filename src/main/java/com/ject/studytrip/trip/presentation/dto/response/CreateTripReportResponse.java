package com.ject.studytrip.trip.presentation.dto.response;

import com.ject.studytrip.trip.application.dto.TripReportInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateTripReportResponse(@Schema(name = "여행 리포트 ID") Long tripReportId) {
    public static CreateTripReportResponse of(TripReportInfo tripReportInfo) {
        return new CreateTripReportResponse(tripReportInfo.tripReportId());
    }
}
