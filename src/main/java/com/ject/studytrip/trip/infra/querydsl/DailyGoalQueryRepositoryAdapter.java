package com.ject.studytrip.trip.infra.querydsl;

import com.ject.studytrip.trip.domain.model.QDailyGoal;
import com.ject.studytrip.trip.domain.model.QTrip;
import com.ject.studytrip.trip.domain.repository.DailyGoalQueryRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DailyGoalQueryRepositoryAdapter implements DailyGoalQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QDailyGoal dailyGoal = QDailyGoal.dailyGoal;
    private final QTrip trip = QTrip.trip;

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
}
