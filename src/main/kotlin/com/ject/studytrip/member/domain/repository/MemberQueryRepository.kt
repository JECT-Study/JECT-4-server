package com.ject.studytrip.member.domain.repository

import com.ject.studytrip.member.domain.model.MemberRole
import java.util.Optional

interface MemberQueryRepository {
    fun findMemberRoleById(memberId: Long): Optional<MemberRole>
}
