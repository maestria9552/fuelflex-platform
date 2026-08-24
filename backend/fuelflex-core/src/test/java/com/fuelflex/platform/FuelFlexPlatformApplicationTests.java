package com.fuelflex.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "fuelflex.data-initialization.enabled=false"
})
class FuelFlexPlatformApplicationTests {

    @Test
    void contextLoads() {
    }
}
