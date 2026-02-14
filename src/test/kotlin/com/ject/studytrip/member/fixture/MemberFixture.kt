package com.ject.studytrip.member.fixture

import com.ject.studytrip.member.domain.factory.MemberFactory
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.MemberCategory
import org.springframework.test.util.ReflectionTestUtils

class MemberFixture(
    private val kakaoId: String = "12345",
    private val email: String = "studytrip@kakao.com",
    private val profileImage: String = "https://kakao.com/profile.jpg",
    private val nickname: String = "민우",
    private val category: MemberCategory = MemberCategory.STUDENT,
) {
    fun createFromKakao(): Member = MemberFactory.createFromKakao(kakaoId, email, profileImage, nickname, category)

    fun createFromKakao(
        email: String,
        nickname: String,
    ): Member = MemberFactory.createFromKakao(kakaoId, email, profileImage, nickname, category)

    fun createFromKakaoWithId(id: Long): Member = createFromKakao().also { ReflectionTestUtils.setField(it, "id", id) }

    fun createFromKakaoWithoutProfileImage(): Member = MemberFactory.createFromKakao(kakaoId, email, null, nickname, category)
}
