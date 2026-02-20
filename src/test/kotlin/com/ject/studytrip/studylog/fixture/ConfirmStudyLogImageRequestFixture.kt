package com.ject.studytrip.studylog.fixture

import com.ject.studytrip.studylog.presentation.dto.request.ConfirmStudyLogImageRequest

class ConfirmStudyLogImageRequestFixture(
    private val tmpKey: String = "tmp/study-logs/1/test.jpg",
) {
    fun withTmpKey(tmpKey: String): ConfirmStudyLogImageRequestFixture = ConfirmStudyLogImageRequestFixture(tmpKey)

    fun build(): ConfirmStudyLogImageRequest = ConfirmStudyLogImageRequest(tmpKey)
}
