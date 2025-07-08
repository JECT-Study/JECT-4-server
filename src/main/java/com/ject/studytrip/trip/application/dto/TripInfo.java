package com.ject.studytrip.trip.application.dto;

import com.ject.studytrip.global.util.DateUtil;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;

public record TripInfo(
        Long tripId,
        String tripName,
        String tripMemo,
        TripCategory tripCategory,
        String startDate,
        String endDate,
        Integer dDay,
        int totalStamps,
        int completedStamps,
        Integer progress,
        boolean completed,
        String createdAt,
        String updatedAt,
        String deletedAt) {
    public static TripInfo from(Trip trip, Integer dDay, Integer progress) {
        return new TripInfo(
                trip.getId(),
                trip.getName(),
                trip.getMemo(),
                trip.getCategory(),
                DateUtil.formatDate(trip.getStartDate()),
                DateUtil.formatDate(trip.getEndDate()),
                dDay,
                trip.getTotalStamps(),
                trip.getCompletedStamps(),
                progress,
                trip.isCompleted(),
                DateUtil.formatDateTime(trip.getCreatedAt()),
                DateUtil.formatDateTime(trip.getUpdatedAt()),
                DateUtil.formatDateTime(trip.getDeletedAt()));
    }
}
