package com.ject.studytrip.studylog.fixture;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.studylog.domain.factory.StudyLogFactory;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import org.springframework.test.util.ReflectionTestUtils;

public class StudyLogFixture {
    private static final String STUDY_LOG_CONTENT = "TEST 학습 로그 내용";

    public static StudyLog createStudyLog(Member member, DailyGoal dailyGoal) {
        return StudyLogFactory.create(member, dailyGoal, STUDY_LOG_CONTENT);
    }

    public static StudyLog createStudyLogWithId(Long id, Member member, DailyGoal dailyGoal) {
        StudyLog studyLog = createStudyLog(member, dailyGoal);
        ReflectionTestUtils.setField(studyLog, "id", id);

        return studyLog;
    }
}
