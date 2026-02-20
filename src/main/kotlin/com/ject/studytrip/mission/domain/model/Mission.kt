package com.ject.studytrip.mission.domain.model

import com.ject.studytrip.global.common.entity.BaseTimeEntity
import com.ject.studytrip.stamp.domain.model.Stamp
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.springframework.util.StringUtils.hasText
import java.time.LocalDateTime

@Entity
class Mission protected constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stamp_id", nullable = false)
    var stamp: Stamp,
    @Column(nullable = false)
    var name: String,
    var completed: Boolean = false,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        fun of(
            stamp: Stamp,
            name: String,
        ): Mission = Mission(stamp, name, false)
    }

    fun updateName(name: String) {
        if (hasText(name)) {
            this.name = name
        }
    }

    fun updateDeletedAt(now: LocalDateTime = LocalDateTime.now()) {
        markDeleted(now)
    }

    fun updateCompleted() {
        this.completed = true
    }

    fun isCompleted(): Boolean = completed
}
