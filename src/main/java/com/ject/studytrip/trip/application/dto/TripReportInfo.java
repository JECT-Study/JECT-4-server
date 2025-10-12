package com.ject.studytrip.trip.application.dto;

import com.ject.studytrip.global.util.DateUtil;
import com.ject.studytrip.trip.domain.model.TripReport;

public record TripReportInfo(
        Long tripReportId,
        String title,
        String content,
        String startDate,
        String endDate,
        long completedMissionCount,
        long totalFocusHours,
        long studyDays,
        String imageTitle,
        String imageUrl,
        String createdAt,
        String updatedAt,
        String deletedAt) {
    public static TripReportInfo from(TripReport tripReport) {
        return new TripReportInfo(
                tripReport.getId(),
                tripReport.getTitle(),
                tripReport.getContent(),
                tripReport.getStartDate(),
                tripReport.getEndDate(),
                tripReport.getCompletedMissionCount(),
                tripReport.getTotalFocusHours(),
                tripReport.getStudyDays(),
                tripReport.getImageTitle(),
                tripReport.getImageUrl(),
                DateUtil.formatDateTime(tripReport.getCreatedAt()),
                DateUtil.formatDateTime(tripReport.getUpdatedAt()),
                DateUtil.formatDateTime(tripReport.getDeletedAt()));
    }
}
