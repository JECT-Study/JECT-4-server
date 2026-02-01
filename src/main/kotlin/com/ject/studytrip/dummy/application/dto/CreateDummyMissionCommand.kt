package com.ject.studytrip.dummy.application.dto

data class CreateDummyMissionCommand(
    val name: String,
) {
    companion object {
        @JvmStatic
        fun of(name: String): CreateDummyMissionCommand = CreateDummyMissionCommand(name)
    }
}
