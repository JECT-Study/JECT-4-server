package com.ject.studytrip.member.infra.jpa;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.SocialProvider;
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
    public boolean existsBySocialProviderAndSocialId(
            SocialProvider socialProvider, String socialId) {
        return memberJpaRepository.existsBySocialProviderAndSocialId(socialProvider, socialId);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id);
    }

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }

    @Override
    public Optional<Member> findByIdAndDeletedAtIsNull(Long id) {
        return memberJpaRepository.findByIdAndDeletedAtIsNull(id);
    }
}
