package com.ject.studytrip.member.infra.querydsl;

import static com.ject.studytrip.member.domain.model.QMember.member;

import com.ject.studytrip.member.domain.repository.MemberCommandRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberCommandRepositoryAdapter implements MemberCommandRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public long deleteAllByDeletedAtIsNotNull() {
        return queryFactory.delete(member).where(member.deletedAt.isNotNull()).execute();
    }
}
