package com.ject.studytrip.dummy.application.dto

import java.time.LocalDate

data class CreateDummyStampCommand(
    val name: String,
    val stampOrder: Int,
    val endDate: LocalDate?,
) {
    companion object {
        @JvmStatic
        fun of(
            name: String,
            stampOrder: Int,
            endDate: LocalDate?,
        ): CreateDummyStampCommand = CreateDummyStampCommand(name, stampOrder, endDate)
    }
}
