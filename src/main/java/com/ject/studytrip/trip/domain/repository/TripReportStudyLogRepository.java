package com.ject.studytrip.trip.domain.repository;

import com.ject.studytrip.trip.domain.model.TripReportStudyLog;
import java.util.List;

public interface TripReportStudyLogRepository {
    void saveAll(List<TripReportStudyLog> tripReportStudyLogs);
}
