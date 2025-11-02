package com.ject.studytrip.trip.application.service;

import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.trip.domain.factory.TripReportStudyLogFactory;
import com.ject.studytrip.trip.domain.model.TripReport;
import com.ject.studytrip.trip.domain.model.TripReportStudyLog;
import com.ject.studytrip.trip.domain.repository.TripReportStudyLogCommandRepository;
import com.ject.studytrip.trip.domain.repository.TripReportStudyLogRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripReportStudyLogCommandService {
    private final TripReportStudyLogRepository tripReportStudyLogRepository;
    private final TripReportStudyLogCommandRepository tripReportStudyLogCommandRepository;

    public void createTripReportStudyLogs(TripReport tripReport, List<StudyLog> studyLogs) {
        List<TripReportStudyLog> tripReportStudyLogs =
                studyLogs.stream()
                        .map(studyLog -> TripReportStudyLogFactory.create(tripReport, studyLog))
                        .toList();

        tripReportStudyLogRepository.saveAll(tripReportStudyLogs);
    }

    public long hardDeleteTripReportStudyLogsOwnedByDeletedMember() {
        return tripReportStudyLogCommandRepository.deleteAllByDeletedMemberOwner();
    }

    public long hardDeleteTripReportStudyLogsByMember(Long memberId) {
        return tripReportStudyLogCommandRepository.deleteAllByMemberId(memberId);
    }
}
