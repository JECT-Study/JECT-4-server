package com.ject.studytrip.trip.presentation.dto.response;

import com.ject.studytrip.trip.application.dto.TripReportInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record LoadTripReportsResponse(
        TripReportSummary summary, List<LoadTripReportInfoResponse> tripReports) {
    public static LoadTripReportsResponse of(List<TripReportInfo> tripReportInfos) {
        return new LoadTripReportsResponse(
                TripReportSummary.of(tripReportInfos),
                tripReportInfos.stream().map(LoadTripReportInfoResponse::of).toList());
    }

    private record TripReportSummary(
            @Schema(description = "여행 완료 수") long completedTripCount,
            @Schema(description = "누적 학습 시간") long totalFocusHours,
            @Schema(description = "가장 긴 학습 시간") long longestFocusHours) {
        private static TripReportSummary of(List<TripReportInfo> tripReportInfos) {
            return new TripReportSummary(
                    tripReportInfos.size(),
                    tripReportInfos.stream().mapToLong(TripReportInfo::totalFocusHours).sum(),
                    tripReportInfos.stream()
                            .mapToLong(TripReportInfo::totalFocusHours)
                            .max()
                            .orElse(0));
        }
    }

    private record LoadTripReportInfoResponse(
            @Schema(description = "여행 리포트 ID") Long tripReportId,
            @Schema(description = "여행 시작일 (여행 회고)") String startDate,
            @Schema(description = "여행 종료일 (여행 회고)") String endDate,
            @Schema(description = "총 학습 시간") long totalFocusHours,
            @Schema(description = "여행 리포트 이미지 URL") String imageUrl) {
        private static LoadTripReportInfoResponse of(TripReportInfo tripReportInfo) {
            return new LoadTripReportInfoResponse(
                    tripReportInfo.tripReportId(),
                    tripReportInfo.startDate(),
                    tripReportInfo.endDate(),
                    tripReportInfo.totalFocusHours(),
                    tripReportInfo.imageUrl());
        }
    }
}
