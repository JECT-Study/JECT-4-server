package com.ject.studytrip.dummy.application.dto;

import com.ject.studytrip.global.util.DateUtil;
import com.ject.studytrip.stamp.domain.model.Stamp;

public record DummyStampInfo(
        Long stampId,
        String stampName,
        int stampOrder,
        String endDate,
        int totalMissions,
        int completedMissions,
        boolean completed,
        String createdAt,
        String updatedAt,
        String deletedAt) {
    public static DummyStampInfo from(Stamp stamp) {
        return new DummyStampInfo(
                stamp.getId(),
                stamp.getName(),
                stamp.getStampOrder(),
                DateUtil.formatDate(stamp.getEndDate()),
                stamp.getTotalMissions(),
                stamp.getCompletedMissions(),
                stamp.isCompleted(),
                DateUtil.formatDateTime(stamp.getCreatedAt()),
                DateUtil.formatDateTime(stamp.getUpdatedAt()),
                DateUtil.formatDateTime(stamp.getDeletedAt()));
    }
}
