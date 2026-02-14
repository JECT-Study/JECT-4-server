package com.ject.studytrip.member.fixture

import com.ject.studytrip.member.presentation.dto.request.ConfirmProfileImageRequest

class ConfirmProfileImageRequestFixture(
    private val tmpKey: String = "tmp/members/1/test.jpg",
) {
    fun withTmpKey(tmpKey: String): ConfirmProfileImageRequestFixture = ConfirmProfileImageRequestFixture(tmpKey)

    fun build(): ConfirmProfileImageRequest = ConfirmProfileImageRequest(tmpKey)
}
