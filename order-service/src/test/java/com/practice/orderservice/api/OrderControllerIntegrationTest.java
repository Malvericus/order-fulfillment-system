package com.practice.orderservice.api;

import com.practice.orderservice.OrderServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against the already-running docker-compose Postgres/Kafka (localhost:5432 / localhost:9092),
 * not an ephemeral Testcontainers instance — Testcontainers' bundled docker-java client cannot
 * negotiate with Rancher Desktop's engine on this machine (fixed API version 1.32 vs required 1.41+).
 * Requires `docker compose up -d postgres kafka` (or the full stack) to be running before this test.
 */
@SpringBootTest(classes = OrderServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @Test
    void createOrder_returns201AndPersistsOrder() {
        var request = Map.of(
                "customerId", "cust-1",
                "items", List.of(Map.of("sku", "sku-1", "quantity", 2)));

        ResponseEntity<Map> response = rest.postForEntity("/orders", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("customerId", "cust-1");
        assertThat(response.getBody()).containsEntry("status", "CREATED");
    }

    @Test
    void createOrder_withBlankCustomerId_returns400() {
        var request = Map.of("customerId", "", "items", List.of(Map.of("sku", "sku-1", "quantity", 2)));

        ResponseEntity<Map> response = rest.postForEntity("/orders", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createOrder_withEmptyItems_returns400() {
        var request = Map.of("customerId", "cust-1", "items", List.of());

        ResponseEntity<Map> response = rest.postForEntity("/orders", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
