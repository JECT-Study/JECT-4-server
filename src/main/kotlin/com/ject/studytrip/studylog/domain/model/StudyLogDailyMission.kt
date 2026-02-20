package com.ject.studytrip.studylog.domain.model

import com.ject.studytrip.global.common.entity.BaseTimeEntity
import com.ject.studytrip.mission.domain.model.DailyMission
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class StudyLogDailyMission protected constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_log_id", nullable = false)
    var studyLog: StudyLog,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_mission_id", nullable = false)
    var dailyMission: DailyMission,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        fun of(
            studyLog: StudyLog,
            dailyMission: DailyMission,
        ): StudyLogDailyMission = StudyLogDailyMission(studyLog, dailyMission)
    }
}
