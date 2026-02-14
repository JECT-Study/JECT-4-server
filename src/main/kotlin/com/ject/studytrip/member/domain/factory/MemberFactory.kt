package com.ject.studytrip.member.domain.factory

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.MemberCategory
import com.ject.studytrip.member.domain.model.MemberRole
import com.ject.studytrip.member.domain.model.SocialProvider

object MemberFactory {
    @JvmStatic
    fun createFromKakao(
        kakaoId: String,
        email: String,
        profileImage: String?,
        nickname: String,
        category: MemberCategory,
    ): Member = Member.of(SocialProvider.KAKAO, kakaoId, email, nickname, profileImage, category, MemberRole.ROLE_USER)
}
