package com.ject.studytrip.mission.domain.repository;

public interface MissionCommandRepository {
    boolean existsByStampIdAndCompletedIsFalseAndDeletedAtIsNull(Long stampId);

    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedStampOwner();

    long deleteAllByMemberId(Long memberId);
}
