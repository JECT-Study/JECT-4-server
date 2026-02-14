package com.ject.studytrip.member.infra.jpa

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.SocialProvider
import com.ject.studytrip.member.domain.repository.MemberRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class MemberRepositoryAdapter(
    private val memberJpaRepository: MemberJpaRepository,
) : MemberRepository {
    override fun findBySocialProviderAndSocialId(
        socialProvider: SocialProvider,
        socialId: String,
    ): Optional<Member> = memberJpaRepository.findBySocialProviderAndSocialId(socialProvider, socialId)

    override fun findById(memberId: Long): Optional<Member> = memberJpaRepository.findById(memberId)

    override fun existsBySocialProviderAndSocialId(
        socialProvider: SocialProvider,
        socialId: String,
    ): Boolean = memberJpaRepository.existsBySocialProviderAndSocialId(socialProvider, socialId)

    override fun save(member: Member): Member = memberJpaRepository.save(member)

    override fun deleteById(memberId: Long) = memberJpaRepository.deleteById(memberId)
}
