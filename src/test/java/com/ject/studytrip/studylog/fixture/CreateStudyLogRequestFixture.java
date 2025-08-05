package com.ject.studytrip.studylog.fixture;

import com.ject.studytrip.studylog.presentation.dto.request.CreateStudyLogRequest;
import java.util.List;

public class CreateStudyLogRequestFixture {
    private int totalFocusTimeInMinutes = 75;
    private List<Long> selectedDailyMissionIds = List.of(1L);
    private String content = "TEST 학습 로그 내용";

    public CreateStudyLogRequestFixture withTotalFocusTimeInMinutes(int totalFocusTimeInMinutes) {
        this.totalFocusTimeInMinutes = totalFocusTimeInMinutes;
        return this;
    }

    public CreateStudyLogRequestFixture withSelectedDailyMissionIds(List<Long> ids) {
        this.selectedDailyMissionIds = ids;
        return this;
    }

    public CreateStudyLogRequestFixture withContent(String content) {
        this.content = content;
        return this;
    }

    public CreateStudyLogRequest build() {
        return new CreateStudyLogRequest(totalFocusTimeInMinutes, selectedDailyMissionIds, content);
    }
}
