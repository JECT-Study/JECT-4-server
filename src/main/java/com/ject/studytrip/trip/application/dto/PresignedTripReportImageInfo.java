package com.ject.studytrip.trip.application.dto;

public record PresignedTripReportImageInfo(Long tripReportId, String tmpKey, String presignedUrl) {
    public static PresignedTripReportImageInfo of(
            Long tripReportId, String tmpKey, String presignedUrl) {
        return new PresignedTripReportImageInfo(tripReportId, tmpKey, presignedUrl);
    }
}
