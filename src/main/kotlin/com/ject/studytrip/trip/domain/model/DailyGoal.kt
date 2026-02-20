package com.ject.studytrip.trip.domain.model

import com.ject.studytrip.global.common.entity.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class DailyGoal protected constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    var trip: Trip,
    @Column(nullable = false)
    var title: String,
    var completed: Boolean = false,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        fun of(
            trip: Trip,
            title: String,
        ): DailyGoal = DailyGoal(trip, title, false)
    }

    fun updateDeletedAt(now: LocalDateTime = LocalDateTime.now()) {
        markDeleted(now)
    }

    fun updateCompleted() {
        this.completed = true
    }

    fun isCompleted(): Boolean = completed
}
