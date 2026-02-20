package com.ject.studytrip.global.exception

import com.ject.studytrip.global.exception.error.ErrorCode

class CustomException(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.message)
