package com.ject.studytrip.member.infra.querydsl

import com.ject.studytrip.member.domain.model.MemberRole
import com.ject.studytrip.member.domain.model.QMember.member
import com.ject.studytrip.member.domain.repository.MemberQueryRepository
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class MemberQueryRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : MemberQueryRepository {
    override fun findMemberRoleById(memberId: Long): Optional<MemberRole> {
        val memberRole: MemberRole? =
            queryFactory
                .select(member.role)
                .from(member)
                .where(member.id.eq(memberId))
                .fetchOne()

        return Optional.ofNullable(memberRole)
    }
}
