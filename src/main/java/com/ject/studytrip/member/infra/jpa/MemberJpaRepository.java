package com.ject.studytrip.member.infra.jpa;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.SocialProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    Optional<Member> findBySocialProviderAndSocialId(
            SocialProvider socialProvider, String socialId);

    boolean existsBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);

    Optional<Member> findByIdAndDeletedAtIsNull(Long id);
}
