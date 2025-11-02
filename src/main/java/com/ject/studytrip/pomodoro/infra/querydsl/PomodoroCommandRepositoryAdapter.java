package com.ject.studytrip.pomodoro.infra.querydsl;

import static com.ject.studytrip.pomodoro.domain.model.QPomodoro.pomodoro;
import static com.ject.studytrip.trip.domain.model.QDailyGoal.dailyGoal;
import static com.ject.studytrip.trip.domain.model.QTrip.trip;

import com.ject.studytrip.pomodoro.domain.repository.PomodoroCommandRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PomodoroCommandRepositoryAdapter implements PomodoroCommandRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public long deleteAllByDeletedAtIsNotNull() {
        return queryFactory.delete(pomodoro).where(pomodoro.deletedAt.isNotNull()).execute();
    }

    @Override
    public long deleteAllByDeletedDailyGoalOwner() {
        return queryFactory
                .delete(pomodoro)
                .where(
                        pomodoro.dailyGoal.id.in(
                                JPAExpressions.select(dailyGoal.id)
                                        .from(dailyGoal)
                                        .where(dailyGoal.deletedAt.isNotNull())))
                .execute();
    }

    @Override
    public long deleteAllByMemberId(Long memberId) {
        List<Long> ids =
                queryFactory
                        .select(pomodoro.id)
                        .from(pomodoro)
                        .join(pomodoro.dailyGoal, dailyGoal)
                        .join(dailyGoal.trip, trip)
                        .where(trip.member.id.eq(memberId))
                        .fetch();

        if (ids.isEmpty()) return 0;

        return queryFactory.delete(pomodoro).where(pomodoro.id.in(ids)).execute();
    }
}
