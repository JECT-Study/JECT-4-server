package com.ject.studytrip.studylog.application.service;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.studylog.domain.factory.StudyLogFactory;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.policy.StudyLogPolicy;
import com.ject.studytrip.studylog.domain.repository.StudyLogCommandRepository;
import com.ject.studytrip.studylog.domain.repository.StudyLogRepository;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyLogCommandService {
    private final StudyLogRepository studyLogRepository;
    private final StudyLogCommandRepository studyLogCommandRepository;

    public StudyLog createStudyLog(Member member, DailyGoal dailyGoal, String content) {
        StudyLog studyLog = StudyLogFactory.create(member, dailyGoal, content);

        return studyLogRepository.save(studyLog);
    }

    public long hardDeleteStudyLogs() {
        return studyLogCommandRepository.deleteAllByDeletedAtIsNotNull();
    }

    public long hardDeleteStudyLogsOwnedByDeletedMember() {
        return studyLogCommandRepository.deleteAllByDeletedMemberOwner();
    }

    public long hardDeleteStudyLogsOwnedByDeletedDailyGoal() {
        return studyLogCommandRepository.deleteAllByDeletedDailyGoalOwner();
    }

    public void updateImageUrl(StudyLog studyLog, String imageUrl) {
        StudyLogPolicy.validateNotDeleted(studyLog);

        studyLog.updateImageUrl(imageUrl);
    }

    public long hardDeleteStudyLogsByMember(Long memberId) {
        return studyLogCommandRepository.deleteByMemberId(memberId);
    }
}
