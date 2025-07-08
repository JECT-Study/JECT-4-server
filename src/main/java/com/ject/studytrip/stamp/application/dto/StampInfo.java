package com.ject.studytrip.stamp.application.dto;

import com.ject.studytrip.global.util.DateUtil;
import com.ject.studytrip.stamp.domain.model.Stamp;

public record StampInfo(
        Long stampId,
        String stampName,
        int stampOrder,
        String deadline,
        boolean completed,
        String createdAt,
        String updatedAt,
        String deletedAt) {
    public static StampInfo from(Stamp stamp) {
        return new StampInfo(
                stamp.getId(),
                stamp.getName(),
                stamp.getStampOrder(),
                DateUtil.formatDate(stamp.getDeadline()),
                stamp.isCompleted(),
                DateUtil.formatDateTime(stamp.getCreatedAt()),
                DateUtil.formatDateTime(stamp.getUpdatedAt()),
                DateUtil.formatDateTime(stamp.getDeletedAt()));
    }
}
