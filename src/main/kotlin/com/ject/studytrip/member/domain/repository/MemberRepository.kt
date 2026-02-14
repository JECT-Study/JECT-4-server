package com.ject.studytrip.member.domain.repository

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.SocialProvider
import java.util.Optional

interface MemberRepository {
    fun findBySocialProviderAndSocialId(
        socialProvider: SocialProvider,
        socialId: String,
    ): Optional<Member>

    fun findById(memberId: Long): Optional<Member>

    fun existsBySocialProviderAndSocialId(
        socialProvider: SocialProvider,
        socialId: String,
    ): Boolean

    fun save(member: Member): Member

    fun deleteById(memberId: Long)
}
