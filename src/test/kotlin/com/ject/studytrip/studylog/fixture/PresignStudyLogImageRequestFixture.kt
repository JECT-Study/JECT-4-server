package com.ject.studytrip.studylog.fixture

import com.ject.studytrip.studylog.presentation.dto.request.PresignStudyLogImageRequest

class PresignStudyLogImageRequestFixture {
    var originFilename: String = "test.jpg"

    fun withOriginFilename(originFilename: String): PresignStudyLogImageRequestFixture = apply { this.originFilename = originFilename }

    fun build(): PresignStudyLogImageRequest = PresignStudyLogImageRequest(originFilename)
}
