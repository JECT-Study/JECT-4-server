package com.ject.studytrip.member.infra.querydsl

import com.ject.studytrip.member.domain.model.QMember.member
import com.ject.studytrip.member.domain.repository.MemberCommandRepository
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class MemberCommandRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : MemberCommandRepository {
    override fun deleteAllByDeletedAtIsNotNull(): Long =
        queryFactory
            .delete(member)
            .where(member.deletedAt.isNotNull)
            .execute()
}
