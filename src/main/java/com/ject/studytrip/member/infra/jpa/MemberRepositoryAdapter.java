package com.ject.studytrip.member.infra.jpa;

import com.ject.studytrip.member.domain.entity.Member;
import com.ject.studytrip.member.domain.entity.SocialProvider;
import com.ject.studytrip.member.domain.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryAdapter implements MemberRepository {
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> findBySocialProviderAndSocialId(
            SocialProvider socialProvider, String socialId) {
        return memberJpaRepository.findBySocialProviderAndSocialId(socialProvider, socialId);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id);
    }

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }
}
