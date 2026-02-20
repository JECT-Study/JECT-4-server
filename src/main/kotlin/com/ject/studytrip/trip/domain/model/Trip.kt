package com.ject.studytrip.trip.domain.model

import com.ject.studytrip.global.common.entity.BaseTimeEntity
import com.ject.studytrip.member.domain.model.Member
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
class Trip protected constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,
    @Column(nullable = false)
    var name: String,
    var memo: String?,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: TripCategory,
    @Column(nullable = false)
    var startDate: LocalDate,
    var endDate: LocalDate?,
    var totalStamps: Int = 0,
    var completedStamps: Int = 0,
    var completed: Boolean = false,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        fun of(
            member: Member,
            name: String,
            memo: String?,
            category: TripCategory,
            endDate: LocalDate?,
            totalStamps: Int,
        ): Trip = Trip(member, name, memo, category, LocalDate.now(), endDate, totalStamps, 0, false)
    }

    fun update(
        name: String?,
        memo: String?,
        category: TripCategory?,
        endDate: LocalDate?,
    ) {
        name?.takeIf { hasText(it) }?.let { this.name = it }
        memo?.takeIf { hasText(it) }?.let { this.memo = it }
        category?.let { this.category = it }
        endDate?.let { this.endDate = it }
    }

    fun updateCompleted() {
        this.completed = true
    }

    fun updateDeletedAt(now: LocalDateTime = LocalDateTime.now()) {
        markDeleted(now)
    }

    fun isCompleted(): Boolean = completed

    fun increaseTotalStamps() {
        this.totalStamps += 1
    }

    fun decreaseTotalStamps() {
        this.totalStamps -= 1
    }

    fun increaseCompletedStamps() {
        this.completedStamps += 1
    }
}
