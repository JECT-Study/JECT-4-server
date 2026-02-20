package com.ject.studytrip.member.domain.repository

interface MemberCommandRepository {
    fun deleteAllByDeletedAtIsNotNull(): Long
}
