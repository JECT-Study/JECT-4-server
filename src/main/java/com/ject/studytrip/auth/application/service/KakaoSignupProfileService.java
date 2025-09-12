package com.ject.studytrip.auth.application.service;

import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.domain.model.KakaoSignupProfile;
import com.ject.studytrip.auth.domain.repository.KakaoSignupProfileRedisRepository;
import com.ject.studytrip.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoSignupProfileService {
    private final KakaoSignupProfileRedisRepository kakaoSignupProfileRedisRepository;

    public String saveAndIssueSignupKey(String socialId, String email, String profileImageUrl) {
        return kakaoSignupProfileRedisRepository.saveAndIssueSignupKey(
                socialId, email, profileImageUrl);
    }

    public KakaoSignupProfile getSignupProfileByKey(String signupKey) {
        if (signupKey == null || signupKey.isBlank()) {
            throw new CustomException(AuthErrorCode.MISSING_KAKAO_SIGNUP_KEY);
        }

        return kakaoSignupProfileRedisRepository
                .findBySignupKey(signupKey)
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_KAKAO_SIGNUP_KEY));
    }

    public void deleteBySignupKey(String signupKey) {
        kakaoSignupProfileRedisRepository.deleteBySignupKey(signupKey);
    }
}
