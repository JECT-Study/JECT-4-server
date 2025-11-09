package com.ject.studytrip.dummy.application.dto;

import java.util.List;

public record DummyMissionsInfo(List<DummyMissionInfo> missionInfos) {
    public static DummyMissionsInfo of(List<DummyMissionInfo> missionInfos) {
        return new DummyMissionsInfo(missionInfos);
    }
}
