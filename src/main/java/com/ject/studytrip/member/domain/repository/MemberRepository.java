package com.ject.studytrip.member.domain.repository;

import com.ject.studytrip.member.domain.entity.Member;
import com.ject.studytrip.member.domain.entity.SocialProvider;
import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findBySocialProviderAndSocialId(
            SocialProvider socialProvider, String socialId);

    Optional<Member> findById(Long id);

    Member save(Member member);
}
