package com.ject.studytrip.stamp.domain.model

import com.ject.studytrip.global.common.entity.BaseTimeEntity
import com.ject.studytrip.trip.domain.model.Trip
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.springframework.util.StringUtils.hasText
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class Stamp protected constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    var trip: Trip,
    @Column(nullable = false)
    var name: String,
    var stampOrder: Int,
    var endDate: LocalDate?,
    var totalMissions: Int = 0,
    var completedMissions: Int = 0,
    var completed: Boolean = false,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        fun of(
            trip: Trip,
            name: String,
            stampOrder: Int,
            endDate: LocalDate?,
        ): Stamp = Stamp(trip, name, stampOrder, endDate, 0, 0, false)
    }

    fun updateName(name: String) {
        if (hasText(name)) {
            this.name = name
        }
    }

    fun updateStampOrder(newOrder: Int) {
        this.stampOrder = newOrder
    }

    fun updateEndDate(endDate: LocalDate) {
        this.endDate = endDate
    }

    fun updateCompleted() {
        this.completed = true
    }

    fun updateDeletedAt(now: LocalDateTime = LocalDateTime.now()) {
        markDeleted(now)
    }

    fun increaseTotalMissions() {
        this.totalMissions += 1
    }

    fun decreaseTotalMissions() {
        this.totalMissions -= 1
    }

    fun increaseCompletedMissions(count: Int) {
        this.completedMissions += count
    }

    fun isCompleted(): Boolean = completed
}
