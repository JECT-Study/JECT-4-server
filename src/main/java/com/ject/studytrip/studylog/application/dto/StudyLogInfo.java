package com.ject.studytrip.studylog.application.dto;

import com.ject.studytrip.global.util.DateUtil;
import com.ject.studytrip.studylog.domain.model.StudyLog;

public record StudyLogInfo(
        Long studyLogId,
        String title,
        String content,
        String imageUrl,
        String createdAt,
        String updatedAt,
        String deletedAt) {
    public static StudyLogInfo from(StudyLog studyLog) {
        return new StudyLogInfo(
                studyLog.getId(),
                studyLog.getTitle(),
                studyLog.getContent(),
                studyLog.getImageUrl(),
                DateUtil.formatDateTime(studyLog.getCreatedAt()),
                DateUtil.formatDateTime(studyLog.getUpdatedAt()),
                DateUtil.formatDateTime(studyLog.getDeletedAt()));
    }
}
