package com.ject.studytrip.studylog.fixture

import com.ject.studytrip.studylog.presentation.dto.request.ConfirmStudyLogImageRequest

class ConfirmStudyLogImageRequestFixture {
    var tmpKey: String = "tmp/study-logs/1/test.jpg"

    fun withTmpKey(tmpKey: String): ConfirmStudyLogImageRequestFixture = apply { this.tmpKey = tmpKey }

    fun build(): ConfirmStudyLogImageRequest = ConfirmStudyLogImageRequest(tmpKey)
}
