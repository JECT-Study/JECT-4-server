package com.ject.studytrip.trip.domain.model;

import static jakarta.persistence.FetchType.LAZY;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class TripReportStudyLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "trip_report_id")
    private TripReport tripReport;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "study_log_id")
    private StudyLog studyLog;

    public static TripReportStudyLog of(TripReport tripReport, StudyLog studyLog) {
        return TripReportStudyLog.builder().tripReport(tripReport).studyLog(studyLog).build();
    }
}
