package com.ject.studytrip.studylog.infra.querydsl;

import static com.ject.studytrip.member.domain.model.QMember.member;
import static com.ject.studytrip.studylog.domain.model.QStudyLog.studyLog;
import static com.ject.studytrip.trip.domain.model.QDailyGoal.dailyGoal;

import com.ject.studytrip.studylog.domain.repository.StudyLogCommandRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyLogCommandRepositoryAdapter implements StudyLogCommandRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public long deleteAllByDeletedAtIsNotNull() {
        return queryFactory.delete(studyLog).where(studyLog.deletedAt.isNotNull()).execute();
    }

    @Override
    public long deleteAllByDeletedMemberOwner() {
        return queryFactory
                .delete(studyLog)
                .where(
                        studyLog.member.id.in(
                                JPAExpressions.select(member.id)
                                        .from(member)
                                        .where(member.deletedAt.isNotNull())))
                .execute();
    }

    @Override
    public long deleteAllByDeletedDailyGoalOwner() {
        return queryFactory
                .delete(studyLog)
                .where(
                        studyLog.dailyGoal.id.in(
                                JPAExpressions.select(dailyGoal.id)
                                        .from(dailyGoal)
                                        .where(dailyGoal.deletedAt.isNotNull())))
                .execute();
    }

    @Override
    public long deleteByMemberId(Long memberId) {
        return queryFactory.delete(studyLog).where(studyLog.member.id.eq(memberId)).execute();
    }
}
