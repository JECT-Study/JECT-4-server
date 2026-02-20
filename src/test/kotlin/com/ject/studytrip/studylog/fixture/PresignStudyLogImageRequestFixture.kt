package com.ject.studytrip.studylog.fixture

import com.ject.studytrip.studylog.presentation.dto.request.PresignStudyLogImageRequest

class PresignStudyLogImageRequestFixture(
    private val originFilename: String = "test.jpg",
) {
    fun withOriginFilename(originFilename: String): PresignStudyLogImageRequestFixture = PresignStudyLogImageRequestFixture(originFilename)

    fun build(): PresignStudyLogImageRequest = PresignStudyLogImageRequest(originFilename)
}
