package com.practice.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewaySecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void requestWithoutToken_isRejectedWith401() {
        webTestClient.get().uri("/api/orders/123")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void actuatorHealth_isAccessibleWithoutToken() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }
}
