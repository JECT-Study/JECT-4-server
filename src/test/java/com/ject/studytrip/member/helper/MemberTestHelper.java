package com.ject.studytrip.member.helper;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.repository.MemberRepository;
import com.ject.studytrip.member.fixture.MemberFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberTestHelper {
    private final MemberRepository memberRepository;

    @Autowired
    public MemberTestHelper(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member saveMember() {
        Member member = MemberFixture.createMemberFromKakao();
        return memberRepository.save(member);
    }

    public Member saveMember(String email, String nickname) {
        Member member = MemberFixture.createMemberFromKakao(email, nickname);
        return memberRepository.save(member);
    }

    public void deleteMemberById(Long memberId) {
        memberRepository.deleteById(memberId);
    }
}
