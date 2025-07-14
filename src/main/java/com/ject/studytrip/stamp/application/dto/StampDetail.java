package com.ject.studytrip.stamp.application.dto;

import com.ject.studytrip.mission.application.dto.MissionInfo;
import java.util.List;

public record StampDetail(StampInfo stampInfo, List<MissionInfo> missionInfos) {
    public static StampDetail from(StampInfo stampInfo, List<MissionInfo> missionInfos) {
        return new StampDetail(stampInfo, missionInfos);
    }
}
