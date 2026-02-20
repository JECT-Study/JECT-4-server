package com.ject.studytrip.member.fixture

import com.ject.studytrip.member.application.dto.CreateMemberCommand

class CreateMemberCommandFixture(
    private val socialId: String = "12345",
    private val email: String = "studytrip@gmail.com",
    private val profileImage: String? = "https://kakao.com/profile.jpg",
    private val nickname: String = "TEST 멤버 닉네임",
    private val category: String = "STUDENT",
) {
    fun withProfileImage(profileImage: String?): CreateMemberCommandFixture =
        CreateMemberCommandFixture(socialId, email, profileImage, nickname, category)

    fun withNickname(nickname: String): CreateMemberCommandFixture =
        CreateMemberCommandFixture(socialId, email, profileImage, nickname, category)

    fun build(): CreateMemberCommand = CreateMemberCommand(socialId, email, profileImage, nickname, category)
}
