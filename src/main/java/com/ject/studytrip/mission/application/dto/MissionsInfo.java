package com.ject.studytrip.mission.application.dto;

import java.util.List;

public record MissionsInfo(List<MissionInfo> missionInfos) {
    public static MissionsInfo of(List<MissionInfo> missionInfos) {
        return new MissionsInfo(missionInfos);
    }
}
