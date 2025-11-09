package com.ject.studytrip.dummy.application.dto;

import com.ject.studytrip.global.util.DateUtil;
import com.ject.studytrip.mission.domain.model.Mission;

public record DummyMissionInfo(
        Long missionId,
        String missionName,
        boolean completed,
        String createdAt,
        String updatedAt,
        String deletedAt) {
    public static DummyMissionInfo from(Mission mission) {
        return new DummyMissionInfo(
                mission.getId(),
                mission.getName(),
                mission.isCompleted(),
                DateUtil.formatDateTime(mission.getCreatedAt()),
                DateUtil.formatDateTime(mission.getUpdatedAt()),
                DateUtil.formatDateTime(mission.getDeletedAt()));
    }
}
