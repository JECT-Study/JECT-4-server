package com.ject.studytrip.trip.fixture

import com.ject.studytrip.trip.presentation.dto.request.ConfirmTripReportImageRequest

class ConfirmTripReportImageRequestFixture {
    var tmpKey: String = "tmp/trip-reports/1/test.jpg"

    fun withTmpKey(tmpKey: String): ConfirmTripReportImageRequestFixture = apply { this.tmpKey = tmpKey }

    fun build(): ConfirmTripReportImageRequest = ConfirmTripReportImageRequest(tmpKey)
}
