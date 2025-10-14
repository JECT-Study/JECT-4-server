package com.ject.studytrip.stamp.application.dto;

import java.util.List;

public record StampsInfo(List<StampInfo> stampsInfos) {
    public static StampsInfo of(List<StampInfo> stampsInfos) {
        return new StampsInfo(stampsInfos);
    }
}
