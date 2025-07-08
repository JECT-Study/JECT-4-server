package com.ject.studytrip;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestPropertySource(locations = "file:.env")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE) // 테스트 시 내장된 인메모리 DB를 사용하지 않는다는 설정
@SpringBootTest
class StudytripApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("🌐 DB_HOST = " + System.getenv("DB_HOST"));
    }
}
