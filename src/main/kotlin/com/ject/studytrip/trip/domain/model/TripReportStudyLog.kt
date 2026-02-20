package com.ject.studytrip.trip.domain.model

import com.ject.studytrip.global.common.entity.BaseTimeEntity
import com.ject.studytrip.studylog.domain.model.StudyLog
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class TripReportStudyLog protected constructor(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_report_id")
    var tripReport: TripReport,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_log_id")
    var studyLog: StudyLog,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        fun of(
            tripReport: TripReport,
            studyLog: StudyLog,
        ): TripReportStudyLog = TripReportStudyLog(tripReport, studyLog)
    }
}
