package com.ject.studytrip.mission.domain.repository;

import com.ject.studytrip.mission.domain.model.Mission;
import java.util.List;

public interface MissionQueryRepository {
    List<Mission> findAllByIdsInFetchJoinStamp(List<Long> ids);

    boolean existsByStampIdAndCompletedIsFalseAndDeletedAtIsNull(Long stampId);

    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedStampOwner();
}
