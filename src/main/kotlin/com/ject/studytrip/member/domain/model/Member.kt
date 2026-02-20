package com.ject.studytrip.member.domain.model

import com.ject.studytrip.global.common.entity.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.util.StringUtils.hasText
import java.time.LocalDateTime

@Entity
class Member protected constructor(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var socialProvider: SocialProvider,
    @Column(nullable = false)
    var socialId: String,
    @Column(nullable = false, unique = true)
    var email: String,
    @Column(nullable = false)
    var nickname: String,
    var profileImage: String?,
    @Enumerated(EnumType.STRING)
    var category: MemberCategory,
    @Enumerated(EnumType.STRING)
    var role: MemberRole,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        fun of(
            socialProvider: SocialProvider,
            socialId: String,
            email: String,
            nickname: String,
            profileImage: String?,
            category: MemberCategory,
            role: MemberRole,
        ): Member = Member(socialProvider, socialId, email, nickname, profileImage, category, role)
    }

    fun update(
        nickname: String?,
        category: MemberCategory?,
    ) {
        nickname
            ?.takeIf { hasText(it) && it != this.nickname }
            ?.let { this.nickname = it }

        category
            ?.takeIf { it != this.category }
            ?.let { this.category = it }
    }

    fun updateProfileImage(profileImage: String) {
        if (hasText(profileImage)) {
            this.profileImage = profileImage
        }
    }

    fun updateDeletedAt(now: LocalDateTime = LocalDateTime.now()) {
        markDeleted(now)
    }

    fun restoreDeletedAt() {
        this.deletedAt = null
    }
}
