package com.ject.studytrip.member.fixture

import com.ject.studytrip.member.presentation.dto.request.PresignProfileImageRequest

class PresignProfileImageRequestFixture(
    private val originFilename: String = "test.jpg",
) {
    fun withOriginFilename(originFilename: String): PresignProfileImageRequestFixture = PresignProfileImageRequestFixture(originFilename)

    fun build(): PresignProfileImageRequest = PresignProfileImageRequest(originFilename)
}
