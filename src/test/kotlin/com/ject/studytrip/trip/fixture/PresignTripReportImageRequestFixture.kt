package com.ject.studytrip.trip.fixture

import com.ject.studytrip.trip.presentation.dto.request.PresignTripReportImageRequest

class PresignTripReportImageRequestFixture {
    var originFilename: String = "test.jpg"

    fun withOriginFilename(originFilename: String): PresignTripReportImageRequestFixture = apply { this.originFilename = originFilename }

    fun build(): PresignTripReportImageRequest = PresignTripReportImageRequest(originFilename)
}
