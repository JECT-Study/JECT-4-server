package com.ject.studytrip.studylog.presentation.dto.response;

import com.ject.studytrip.studylog.application.dto.StudyLogInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateStudyLogResponse(@Schema(description = "생성된 학습 로그 ID") Long studyLogId) {
    public static CreateStudyLogResponse of(StudyLogInfo info) {
        return new CreateStudyLogResponse(info.studyLogId());
    }
}
