package com.ject.studytrip.member.domain.repository;

import com.ject.studytrip.member.domain.model.MemberRole;

public interface MemberQueryRepository {
    MemberRole findMemberRoleById(Long memberId);
}
