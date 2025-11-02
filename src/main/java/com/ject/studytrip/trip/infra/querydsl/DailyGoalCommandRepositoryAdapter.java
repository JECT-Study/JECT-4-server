package com.ject.studytrip.trip.infra.querydsl;

import static com.ject.studytrip.trip.domain.model.QDailyGoal.dailyGoal;
import static com.ject.studytrip.trip.domain.model.QTrip.trip;

import com.ject.studytrip.trip.domain.repository.DailyGoalCommandRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DailyGoalCommandRepositoryAdapter implements DailyGoalCommandRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public long deleteAllByDeletedAtIsNotNull() {
        return queryFactory.delete(dailyGoal).where(dailyGoal.deletedAt.isNotNull()).execute();
    }

    @Override
    public long deleteAllByDeletedTripOwner() {
        return queryFactory
                .delete(dailyGoal)
                .where(
                        dailyGoal.trip.id.in(
                                JPAExpressions.select(trip.id)
                                        .from(trip)
                                        .where(trip.deletedAt.isNotNull())))
                .execute();
    }

    @Override
    public long deleteAllByMemberId(Long memberId) {
        List<Long> ids =
                queryFactory
                        .select(dailyGoal.id)
                        .from(dailyGoal)
                        .join(dailyGoal.trip, trip)
                        .where(trip.member.id.eq(memberId))
                        .fetch();

        if (ids.isEmpty()) return 0;

        return queryFactory.delete(dailyGoal).where(dailyGoal.id.in(ids)).execute();
    }
}
