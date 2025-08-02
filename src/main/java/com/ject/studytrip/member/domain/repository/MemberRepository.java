package com.ject.studytrip.member.domain.repository;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.SocialProvider;
import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findBySocialProviderAndSocialId(
            SocialProvider socialProvider, String socialId);

    boolean existsBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);

    Optional<Member> findById(Long id);

    Member save(Member member);

    Optional<Member> findByIdAndDeletedAtIsNull(Long id);

    void deleteById(Long id);
}
