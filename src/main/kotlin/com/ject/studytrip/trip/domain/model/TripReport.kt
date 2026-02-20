package com.ject.studytrip.trip.domain.model

import com.ject.studytrip.global.common.entity.BaseTimeEntity
import com.ject.studytrip.member.domain.model.Member
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
class TripReport protected constructor(
    var title: String?,
    @Column(nullable = false)
    var content: String,
    @Column(nullable = false)
    var startDate: String,
    var endDate: String?,
    @Column(nullable = false)
    var studyLogCount: Long,
    @Column(nullable = false)
    var totalFocusHours: Long,
    @Column(nullable = false)
    var studyDays: Long,
    var imageTitle: String?,
    var imageUrl: String? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        fun of(
            member: Member,
            title: String,
            content: String,
            startDate: String,
            endDate: String?,
            studyLogCount: Long,
            totalFocusHours: Long,
            studyDays: Long,
            imageTitle: String?,
        ): TripReport = TripReport(title, content, startDate, endDate, studyLogCount, totalFocusHours, studyDays, imageTitle, null, member)
    }

    fun updateImageUrl(imageUrl: String) {
        if (hasText(imageUrl)) {
            this.imageUrl = imageUrl
        }
    }

    fun updateDeletedAt(now: LocalDateTime = LocalDateTime.now()) {
        markDeleted(now)
    }
}
