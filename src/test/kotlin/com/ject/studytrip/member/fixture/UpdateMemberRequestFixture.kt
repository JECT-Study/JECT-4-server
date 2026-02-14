package com.ject.studytrip.member.fixture

import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest

class UpdateMemberRequestFixture(
    private val nickname: String? = null,
    private val category: String? = null,
) {
    fun withNickname(nickname: String?): UpdateMemberRequestFixture = UpdateMemberRequestFixture(nickname, category)

    fun withCategory(category: String?): UpdateMemberRequestFixture = UpdateMemberRequestFixture(nickname, category)

    fun build(): UpdateMemberRequest = UpdateMemberRequest(nickname, category)
}
