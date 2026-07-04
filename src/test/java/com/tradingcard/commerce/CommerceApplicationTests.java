package com.tradingcard.commerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9999/realms/test",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9999/realms/test/protocol/openid-connect/certs"
})
class CommerceApplicationTests {

    @Test
    void contextLoads() {
    }
}

