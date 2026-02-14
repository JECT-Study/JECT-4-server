package com.ject.studytrip

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(classes = [StudytripApplication::class])
@ActiveProfiles("test")
@TestPropertySource(locations = ["file:.env"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@AutoConfigureMockMvc
abstract class BaseIntegrationTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    /**
     * 응답 본문(JSON)을 지정한 클래스 타입으로 변환하는 메서드, 테스트 응답 결과를 객체로 파싱해 내용 검증에 활용할 수 있음
     *
     * @param result MockMvc 응답 결과
     * @param clazz 변환할 클래스 타입
     * @return 파싱된 응답 객체
     */
    protected fun <T> parseResponse(
        result: ResultActions,
        clazz: Class<T>,
    ): T {
        val content = result.andReturn().response.contentAsString
        return objectMapper.readValue(content, clazz)
    }
}
