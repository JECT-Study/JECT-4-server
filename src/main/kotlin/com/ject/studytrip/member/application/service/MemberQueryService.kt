package com.ject.studytrip.member.application.service

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.member.domain.error.MemberErrorCode
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.MemberRole
import com.ject.studytrip.member.domain.model.SocialProvider
import com.ject.studytrip.member.domain.policy.MemberPolicy
import com.ject.studytrip.member.domain.repository.MemberQueryRepository
import com.ject.studytrip.member.domain.repository.MemberRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class MemberQueryService(
    private val memberRepository: MemberRepository,
    private val memberQueryRepository: MemberQueryRepository,
) {
    fun getMemberBySocialProviderAndSocialId(
        socialProvider: SocialProvider,
        socialId: String,
    ): Optional<Member> =
        memberRepository
            .findBySocialProviderAndSocialId(socialProvider, socialId)
            .map {
                MemberPolicy.validateNotDeleted(it)
                it
            }

    fun getValidMember(memberId: Long): Member {
        val member =
            memberRepository
                .findById(memberId)
                .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        MemberPolicy.validateNotDeleted(member)

        return member
    }

    fun getDeletedMember(memberId: Long): Member {
        val member =
            memberRepository
                .findById(memberId)
                .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        MemberPolicy.validateDeleted(member)

        return member
    }

    fun getMemberRoleByMemberId(memberId: Long): MemberRole =
        memberQueryRepository
            .findMemberRoleById(memberId)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }
}
