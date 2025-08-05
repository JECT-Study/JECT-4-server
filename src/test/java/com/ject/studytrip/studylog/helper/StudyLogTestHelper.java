package com.ject.studytrip.studylog.helper;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.repository.StudyLogRepository;
import com.ject.studytrip.studylog.fixture.StudyLogFixture;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyLogTestHelper {
    @Autowired private StudyLogRepository studyLogRepository;

    public StudyLog saveStudyLog(Member member, DailyGoal dailyGoal) {
        StudyLog studyLog = StudyLogFixture.createStudyLog(member, dailyGoal);
        return studyLogRepository.save(studyLog);
    }
}
