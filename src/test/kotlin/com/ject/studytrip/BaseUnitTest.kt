package com.ject.studytrip

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
abstract class BaseUnitTest {
    protected val objectMapper: ObjectMapper = ObjectMapper()
}
