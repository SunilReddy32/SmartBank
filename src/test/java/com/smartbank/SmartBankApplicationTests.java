package com.smartbank;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// Basic smoke test — verifies the Spring context loads correctly
// This runs in CI to catch any configuration errors early
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.mail.host=smtp.gmail.com",
    "spring.mail.username=test@gmail.com",
    "spring.mail.password=testpassword",
    "DB_PASSWORD=testpassword",
    "MAIL_USERNAME=test@gmail.com",
    "MAIL_PASSWORD=testpassword"
})
class SmartBankApplicationTests {

    @Test
    void contextLoads() {
        // If this test passes, the Spring context loaded successfully
        // — all beans created, all configs valid
    }
}