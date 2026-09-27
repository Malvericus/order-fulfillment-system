package com.practice.insightservice.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InsightControllerTest {

    @Autowired
    TestRestTemplate rest;

    @Test
    void orderInsight_returnsSummaryContainingOrderId() {
        ResponseEntity<Map> response = rest.getForEntity("/insights/orders/order-1", Map.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().get("summary").toString()).contains("order-1");
    }

    @Test
    void dailyDigest_returnsNonEmptySummary() {
        ResponseEntity<Map> response = rest.getForEntity("/insights/daily-digest", Map.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().get("summary").toString()).isNotBlank();
    }
}
