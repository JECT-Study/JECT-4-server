package com.ject.studytrip.studylog.application.dto;

import java.util.List;

public record StudyLogSliceInfo(List<StudyLogDetail> studyLogDetails, boolean hasNext) {
    public static StudyLogSliceInfo of(List<StudyLogDetail> studyLogDetails, boolean hasNext) {
        return new StudyLogSliceInfo(studyLogDetails, hasNext);
    }
}
