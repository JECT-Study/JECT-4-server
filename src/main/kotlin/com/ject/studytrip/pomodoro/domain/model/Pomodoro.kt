package com.ject.studytrip.pomodoro.domain.model

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
class Pomodoro protected constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_goal_id", nullable = false)
    var dailyGoal: DailyGoal,
    var focusDurationInSeconds: Int,
    var focusSessionCount: Int,
    var breakDurationInSeconds: Int,
    var totalFocusTimeInSeconds: Int = 0,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        fun of(
            dailyGoal: DailyGoal,
            focusDurationInSeconds: Int,
            focusSessionCount: Int,
            breakDurationInSeconds: Int,
        ): Pomodoro = Pomodoro(dailyGoal, focusDurationInSeconds, focusSessionCount, breakDurationInSeconds, 0)
    }

    fun updateTotalFocusTimeInSeconds(totalFocusTimeInSeconds: Int) {
        this.totalFocusTimeInSeconds = totalFocusTimeInSeconds
    }

    fun updateDeletedAt(now: LocalDateTime = LocalDateTime.now()) {
        markDeleted(now)
    }
}
