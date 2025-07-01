package com.ject.studytrip.member.helper;

import com.ject.studytrip.member.domain.entity.Member;
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

    public void saveMember() {
        Member member = MemberFixture.createMemberFromKakao();
        memberRepository.save(member);
    }
}
