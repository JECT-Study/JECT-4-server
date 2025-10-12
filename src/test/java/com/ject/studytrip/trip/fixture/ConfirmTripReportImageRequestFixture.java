package com.ject.studytrip.trip.fixture;

import com.ject.studytrip.trip.presentation.dto.request.ConfirmTripReportImageRequest;

public class ConfirmTripReportImageRequestFixture {
    private String tmpKey = "tmp/trip-reports/1/test.jpg";

    public ConfirmTripReportImageRequest build() {
        return new ConfirmTripReportImageRequest(tmpKey);
    }

    public ConfirmTripReportImageRequestFixture withTmpKey(String tmpKey) {
        this.tmpKey = tmpKey;
        return this;
    }
}
