package com.ject.studytrip.trip.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class PresignedTripReportImageResponse(
    @field:Schema(description = "여행 리포트 ID")
    val tripReportId: Long,
    @field:Schema(description = "여행 리포트 이미지 임시키")
    val tmpKey: String,
    @field:Schema(description = "여행 리포트 이미지 업로드용 Presigned URL")
    val presignedUrl: String,
) {
    companion object {
        @JvmStatic
        fun of(
            tripReportId: Long,
            tmpKey: String,
            presignedUrl: String,
        ): PresignedTripReportImageResponse = PresignedTripReportImageResponse(tripReportId, tmpKey, presignedUrl)
    }
}
