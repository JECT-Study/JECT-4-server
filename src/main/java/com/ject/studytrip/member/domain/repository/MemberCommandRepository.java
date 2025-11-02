package com.ject.studytrip.member.domain.repository;

public interface MemberCommandRepository {
    long deleteAllByDeletedAtIsNotNull();
}
