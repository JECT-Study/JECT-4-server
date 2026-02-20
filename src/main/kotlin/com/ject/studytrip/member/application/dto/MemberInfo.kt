package com.ject.studytrip.member.application.dto

import com.ject.studytrip.global.util.DateUtil
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.MemberCategory
import com.ject.studytrip.member.domain.model.MemberRole
import com.ject.studytrip.member.domain.model.SocialProvider

data class MemberInfo(
    val memberId: Long,
    val socialProvider: SocialProvider,
    val socialId: String,
    val email: String,
    val nickname: String,
    val profileImage: String?,
    val category: MemberCategory,
    val role: MemberRole,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
) {
    companion object {
        fun from(member: Member): MemberInfo =
            MemberInfo(
                member.id.requireId(),
                member.socialProvider,
                member.socialId,
                member.email,
                member.nickname,
                member.profileImage,
                member.category,
                member.role,
                DateUtil.formatDateTime(requireNotNull(member.createdAt)),
                DateUtil.formatDateTime(requireNotNull(member.updatedAt)),
                member.deletedAt?.let { DateUtil.formatDateTime(it) },
            )
    }
}
