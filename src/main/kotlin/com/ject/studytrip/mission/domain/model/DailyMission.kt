package com.ject.studytrip.mission.domain.model

import com.ject.studytrip.global.common.entity.BaseTimeEntity
import com.ject.studytrip.trip.domain.model.DailyGoal
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class DailyMission protected constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    var mission: Mission,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_goal_id", nullable = false)
    var dailyGoal: DailyGoal,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        fun of(
            mission: Mission,
            dailyGoal: DailyGoal,
        ): DailyMission = DailyMission(mission, dailyGoal)
    }

    fun updateDeletedAt(now: LocalDateTime = LocalDateTime.now()) {
        markDeleted(now)
    }
}
