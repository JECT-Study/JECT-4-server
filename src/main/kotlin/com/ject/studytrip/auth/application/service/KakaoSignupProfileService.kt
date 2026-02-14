package com.ject.studytrip.auth.application.service

import com.ject.studytrip.auth.application.dto.KakaoSignupProfile
import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.domain.repository.KakaoSignupProfileRedisRepository
import com.ject.studytrip.global.exception.CustomException
import org.springframework.stereotype.Service

@Service
class KakaoSignupProfileService(
    private val kakaoSignupProfileRedisRepository: KakaoSignupProfileRedisRepository,
) {
    fun saveAndIssueSignupKey(
        socialId: String,
        email: String,
        profileImageUrl: String,
    ): String = kakaoSignupProfileRedisRepository.saveAndIssueSignupKey(socialId, email, profileImageUrl)

    fun getSignupProfileByKey(signupKey: String?): KakaoSignupProfile {
        if (signupKey.isNullOrBlank()) {
            throw CustomException(AuthErrorCode.MISSING_KAKAO_SIGNUP_KEY)
        }

        return kakaoSignupProfileRedisRepository
            .findBySignupKey(signupKey)
            .orElseThrow { CustomException(AuthErrorCode.INVALID_KAKAO_SIGNUP_KEY) }
    }

    fun deleteBySignupKey(signupKey: String?) {
        if (signupKey.isNullOrBlank()) return

        kakaoSignupProfileRedisRepository.deleteBySignupKey(signupKey)
    }
}
