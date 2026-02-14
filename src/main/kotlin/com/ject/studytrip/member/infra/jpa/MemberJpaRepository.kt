package com.ject.studytrip.member.infra.jpa

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.SocialProvider
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MemberJpaRepository : JpaRepository<Member, Long> {
    fun findBySocialProviderAndSocialId(
        socialProvider: SocialProvider,
        socialId: String,
    ): Optional<Member>

    fun existsBySocialProviderAndSocialId(
        socialProvider: SocialProvider,
        socialId: String,
    ): Boolean
}
