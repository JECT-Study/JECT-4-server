package com.ject.studytrip.trip.infra.querydsl;

import static com.ject.studytrip.member.domain.model.QMember.member;
import static com.ject.studytrip.trip.domain.model.QTrip.trip;

import com.ject.studytrip.trip.domain.repository.TripCommandRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TripCommandRepositoryAdapter implements TripCommandRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public long deleteAllByDeletedAtIsNotNull() {
        return queryFactory.delete(trip).where(trip.deletedAt.isNotNull()).execute();
    }

    @Override
    public long deleteAllByDeletedMemberOwner() {
        return queryFactory
                .delete(trip)
                .where(
                        trip.member.id.in(
                                JPAExpressions.select(member.id)
                                        .from(member)
                                        .where(member.deletedAt.isNotNull())))
                .execute();
    }

    @Override
    public long deleteAllByMemberId(Long memberId) {
        return queryFactory.delete(trip).where(trip.member.id.eq(memberId)).execute();
    }
}
