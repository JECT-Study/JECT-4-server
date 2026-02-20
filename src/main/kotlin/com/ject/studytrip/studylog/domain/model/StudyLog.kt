package com.ject.studytrip.studylog.domain.model

import com.ject.studytrip.global.common.entity.BaseTimeEntity
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.trip.domain.model.DailyGoal
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
class StudyLog protected constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_goal_id", nullable = false)
    var dailyGoal: DailyGoal,
    @Column(nullable = false)
    var title: String,
    @Column(nullable = false)
    var content: String,
    var imageUrl: String? = null,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        fun of(
            member: Member,
            dailyGoal: DailyGoal,
            content: String,
        ): StudyLog = StudyLog(member, dailyGoal, dailyGoal.title, content, null)
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
