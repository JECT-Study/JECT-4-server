package com.ject.studytrip.mission.application.dto;

import com.ject.studytrip.global.util.DateUtil;
import com.ject.studytrip.mission.domain.model.Mission;

public record MissionInfo(
        Long missionId,
        String missionName,
        boolean completed,
        String createdAt,
        String updatedAt,
        String deletedAt) {
    public static MissionInfo from(Mission mission) {
        return new MissionInfo(
                mission.getId(),
                mission.getName(),
                mission.isCompleted(),
                DateUtil.formatDateTime(mission.getCreatedAt()),
                DateUtil.formatDateTime(mission.getUpdatedAt()),
                DateUtil.formatDateTime(mission.getDeletedAt()));
    }
}
