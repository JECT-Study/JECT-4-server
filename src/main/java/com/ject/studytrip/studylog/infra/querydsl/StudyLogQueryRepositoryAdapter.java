package com.ject.studytrip.studylog.infra.querydsl;

import com.ject.studytrip.studylog.domain.model.QStudyLog;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.repository.StudyLogQueryRepository;
import com.ject.studytrip.trip.domain.model.QDailyGoal;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyLogQueryRepositoryAdapter implements StudyLogQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QStudyLog studyLog = QStudyLog.studyLog;
    private final QDailyGoal dailyGoal = QDailyGoal.dailyGoal;

    @Override
    public long countActiveStudyLogsByMemberId(Long memberId) {
        Long count =
                queryFactory
                        .select(studyLog.count())
                        .from(studyLog)
                        .where(studyLog.member.id.eq(memberId).and(studyLog.deletedAt.isNull()))
                        .fetchOne();

        return Optional.ofNullable(count).orElse(0L);
    }

    @Override
    public Slice<StudyLog> findSliceByTripIdOrderByCreatedAtDesc(Long tripId, Pageable pageable) {
        List<StudyLog> content =
                queryFactory
                        .selectFrom(studyLog)
                        .join(studyLog.dailyGoal, dailyGoal)
                        .where(dailyGoal.trip.id.eq(tripId), dailyGoal.deletedAt.isNull())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize() + 1)
                        .orderBy(studyLog.createdAt.desc())
                        .fetch();

        List<StudyLog> result = content;
        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            result = content.subList(0, pageable.getPageSize());
        }

        return new SliceImpl<>(result, pageable, hasNext);
    }
}
