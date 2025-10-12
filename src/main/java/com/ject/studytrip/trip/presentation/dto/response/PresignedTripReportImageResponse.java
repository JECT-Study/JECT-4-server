package com.ject.studytrip.trip.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignedTripReportImageResponse(
        @Schema(description = "여행 리포트 ID") Long tripReportId,
        @Schema(description = "여행 리포트 이미지 임시키") String tmpKey,
        @Schema(description = "여행 리포트 이미지 업로드용 Presigned URL") String presignedUrl) {
    public static PresignedTripReportImageResponse of(
            Long tripReportId, String tmpKey, String presignedUrl) {
        return new PresignedTripReportImageResponse(tripReportId, tmpKey, presignedUrl);
    }
}
