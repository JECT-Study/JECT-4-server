package com.ject.studytrip.pomodoro.infra.querydsl;

import com.ject.studytrip.pomodoro.domain.model.QPomodoro;
import com.ject.studytrip.pomodoro.domain.repository.PomodoroQueryRepository;
import com.ject.studytrip.trip.domain.model.QDailyGoal;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PomodoroQueryRepositoryAdapter implements PomodoroQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QPomodoro pomodoro = QPomodoro.pomodoro;
    private final QDailyGoal dailyGoal = QDailyGoal.dailyGoal;

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
}
