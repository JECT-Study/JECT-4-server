package com.ject.studytrip.member.helper

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.repository.MemberRepository
import com.ject.studytrip.member.fixture.MemberFixture
import org.springframework.stereotype.Component

@Component
class MemberTestHelper(
    private val memberRepository: MemberRepository,
) {
    fun saveMember(): Member = memberRepository.save(MemberFixture().createFromKakao())

    fun saveNewMember(
        email: String,
        nickname: String,
    ): Member = memberRepository.save(MemberFixture().createFromKakao(email, nickname))

    fun saveDeletedMember(
        email: String,
        nickname: String,
    ): Member = memberRepository.save(MemberFixture().createFromKakao(email, nickname).also { it.updateDeletedAt() })

    fun deleteMemberById(id: Long) = memberRepository.deleteById(id)
}
