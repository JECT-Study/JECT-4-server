package com.ject.studytrip.trip.fixture;

import com.ject.studytrip.trip.presentation.dto.request.PresignTripReportImageRequest;

public class PresignTripReportImageRequestFixture {
    private String originFilename = "test.png";

    public PresignTripReportImageRequest build() {
        return new PresignTripReportImageRequest(originFilename);
    }

    public PresignTripReportImageRequestFixture withOriginFilename(String originFilename) {
        this.originFilename = originFilename;
        return this;
    }
}
