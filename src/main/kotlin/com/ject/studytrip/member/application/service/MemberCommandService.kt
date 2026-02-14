package com.ject.studytrip.member.application.service

import com.ject.studytrip.member.application.dto.CreateMemberCommand
import com.ject.studytrip.member.domain.factory.MemberFactory
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.MemberCategory
import com.ject.studytrip.member.domain.model.SocialProvider
import com.ject.studytrip.member.domain.policy.MemberPolicy
import com.ject.studytrip.member.domain.repository.MemberCommandRepository
import com.ject.studytrip.member.domain.repository.MemberRepository
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest
import org.springframework.stereotype.Service

@Service
class MemberCommandService(
    private val memberRepository: MemberRepository,
    private val memberCommandRepository: MemberCommandRepository,
) {
    fun createMemberFromKakao(command: CreateMemberCommand): Member {
        validateMemberIsUnique(SocialProvider.KAKAO, command.socialId)

        val member =
            MemberFactory.createFromKakao(
                command.socialId,
                command.email,
                command.profileImage,
                command.nickname,
                MemberCategory.from(command.category),
            )

        return memberRepository.save(member)
    }

    fun updateMember(
        member: Member,
        request: UpdateMemberRequest,
    ) {
        val category = request.category?.let { MemberCategory.from(it) }

        member.update(request.nickname, category)
    }

    fun updateProfileImage(
        member: Member,
        profileImage: String,
    ) {
        MemberPolicy.validateNotDeleted(member)

        member.updateProfileImage(profileImage)
    }

    fun deleteMember(member: Member) = member.updateDeletedAt()

    fun restoreMember(member: Member) = member.restoreDeletedAt()

    fun hardDeleteMember(memberId: Long) = memberRepository.deleteById(memberId)

    fun hardDeleteMembers() = memberCommandRepository.deleteAllByDeletedAtIsNotNull()

    private fun validateMemberIsUnique(
        socialProvider: SocialProvider,
        socialId: String,
    ) {
        val exists = memberRepository.existsBySocialProviderAndSocialId(socialProvider, socialId)

        MemberPolicy.validateNotDuplicated(exists)
    }
}
