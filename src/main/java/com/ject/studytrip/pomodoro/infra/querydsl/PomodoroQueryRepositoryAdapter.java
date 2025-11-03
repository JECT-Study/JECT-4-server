package com.ject.studytrip.pomodoro.infra.querydsl;

import com.ject.studytrip.pomodoro.domain.model.QPomodoro;
import com.ject.studytrip.pomodoro.domain.repository.PomodoroQueryRepository;
import com.ject.studytrip.trip.domain.model.QDailyGoal;
import com.ject.studytrip.trip.domain.model.QTrip;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PomodoroQueryRepositoryAdapter implements PomodoroQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QPomodoro pomodoro = QPomodoro.pomodoro;
    private final QDailyGoal dailyGoal = QDailyGoal.dailyGoal;
    private final QTrip trip = QTrip.trip;

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
    public long sumFocusHoursByTripId(Long tripId) {
        Integer totalSeconds =
                queryFactory
                        .select(pomodoro.totalFocusTimeInSeconds.sum())
                        .from(pomodoro)
                        .join(pomodoro.dailyGoal, dailyGoal)
                        .join(dailyGoal.trip, trip)
                        .where(
                                trip.id.eq(tripId),
                                pomodoro.deletedAt.isNull(),
                                dailyGoal.deletedAt.isNull(),
                                trip.deletedAt.isNull())
                        .fetchOne();

        long seconds = totalSeconds == null ? 0L : totalSeconds.longValue();

        return seconds / 3600L; // 정수 시간(내림)
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
