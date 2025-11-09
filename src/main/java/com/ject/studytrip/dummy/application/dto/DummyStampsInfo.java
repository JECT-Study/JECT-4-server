package com.ject.studytrip.dummy.application.dto;

import java.util.List;

public record DummyStampsInfo(List<DummyStampInfo> stampsInfos) {
    public static DummyStampsInfo of(List<DummyStampInfo> stampsInfos) {
        return new DummyStampsInfo(stampsInfos);
    }
}
