package com.ject.studytrip.mission.domain.factory;

import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.stamp.domain.model.Stamp;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MissionFactory {
    public static Mission create(Stamp stamp, String name, String memo, int missionOrder) {
        return Mission.of(stamp, name, memo, missionOrder);
    }
}
