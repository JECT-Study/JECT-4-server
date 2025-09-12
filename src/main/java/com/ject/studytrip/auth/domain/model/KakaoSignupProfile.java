package com.ject.studytrip.auth.domain.model;

public record KakaoSignupProfile(
        String socialId, String socialProvider, String email, String profileImageUrl) {
    public static KakaoSignupProfile of(
            String socialId, String socialProvider, String email, String profileImageUrl) {
        return new KakaoSignupProfile(socialId, socialProvider, email, profileImageUrl);
    }
}
