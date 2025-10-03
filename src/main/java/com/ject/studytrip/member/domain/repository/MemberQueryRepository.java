package com.ject.studytrip.member.domain.repository;

import com.ject.studytrip.member.domain.model.MemberRole;
import java.util.Optional;

public interface MemberQueryRepository {
    Optional<MemberRole> findMemberRoleById(Long memberId);

    long deleteAllByDeletedAtIsNotNull();
}
