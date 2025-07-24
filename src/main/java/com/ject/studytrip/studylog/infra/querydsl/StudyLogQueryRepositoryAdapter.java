package com.ject.studytrip.studylog.infra.querydsl;

import com.ject.studytrip.studylog.domain.model.QStudyLog;
import com.ject.studytrip.studylog.domain.repository.StudyLogQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyLogQueryRepositoryAdapter implements StudyLogQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QStudyLog studyLog = QStudyLog.studyLog;

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
}
