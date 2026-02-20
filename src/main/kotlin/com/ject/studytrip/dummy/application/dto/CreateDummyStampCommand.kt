package com.ject.studytrip.dummy.application.dto

import java.time.LocalDate

data class CreateDummyStampCommand(
    val name: String,
    val stampOrder: Int,
    val endDate: LocalDate?,
)
