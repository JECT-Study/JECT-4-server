package com.ject.studytrip.member.infra.querydsl;

import static com.ject.studytrip.member.domain.model.QMember.member;

import com.ject.studytrip.member.domain.model.MemberRole;
import com.ject.studytrip.member.domain.repository.MemberQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepositoryAdapter implements MemberQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<MemberRole> findMemberRoleById(Long memberId) {
        MemberRole memberRole =
                queryFactory
                        .select(member.role)
                        .from(member)
                        .where(member.id.eq(memberId))
                        .fetchOne();

        return Optional.ofNullable(memberRole);
    }

    @Override
    public long deleteAllByDeletedAtIsNotNull() {
        return queryFactory.delete(member).where(member.deletedAt.isNotNull()).execute();
    }
}
