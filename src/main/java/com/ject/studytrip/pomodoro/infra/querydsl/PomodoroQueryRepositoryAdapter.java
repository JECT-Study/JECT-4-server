package com.ject.studytrip.pomodoro.infra.querydsl;

import static com.ject.studytrip.pomodoro.domain.model.QPomodoro.pomodoro;
import static com.ject.studytrip.trip.domain.model.QDailyGoal.dailyGoal;
import static com.ject.studytrip.trip.domain.model.QTrip.trip;

import com.ject.studytrip.pomodoro.domain.repository.PomodoroQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PomodoroQueryRepositoryAdapter implements PomodoroQueryRepository {
    private final JPAQueryFactory queryFactory;

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
}
