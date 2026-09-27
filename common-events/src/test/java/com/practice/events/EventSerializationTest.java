package com.practice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void orderCreatedEventRoundTripsThroughJson() throws Exception {
        var event = new OrderCreatedEvent(
                "order-1", "cust-1",
                List.of(new OrderCreatedEvent.OrderLine("sku-1", 2)),
                Instant.parse("2026-01-01T00:00:00Z"));

        String json = mapper.writeValueAsString(event);
        OrderCreatedEvent roundTripped = mapper.readValue(json, OrderCreatedEvent.class);

        assertEquals(event, roundTripped);
    }
}
