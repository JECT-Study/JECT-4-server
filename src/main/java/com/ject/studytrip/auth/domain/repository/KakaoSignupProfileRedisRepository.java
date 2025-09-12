package com.ject.studytrip.auth.domain.repository;

import com.ject.studytrip.auth.domain.model.KakaoSignupProfile;
import java.util.Optional;

public interface KakaoSignupProfileRedisRepository {
    String saveAndIssueSignupKey(String socialId, String email, String profileImageUrl);

    Optional<KakaoSignupProfile> findBySignupKey(String signupKey);

    void deleteBySignupKey(String signupKey);
}
