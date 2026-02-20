package com.ject.studytrip.trip.fixture

import com.ject.studytrip.trip.presentation.dto.request.PresignTripReportImageRequest

class PresignTripReportImageRequestFixture(
    private val originFilename: String = "test.jpg",
) {
    fun withOriginFilename(originFilename: String): PresignTripReportImageRequestFixture =
        PresignTripReportImageRequestFixture(originFilename)

    fun build(): PresignTripReportImageRequest = PresignTripReportImageRequest(originFilename)
}
