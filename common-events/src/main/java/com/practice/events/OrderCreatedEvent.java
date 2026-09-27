package com.practice.events;

import java.time.Instant;
import java.util.List;

public record OrderCreatedEvent(String orderId, String customerId, List<OrderLine> items, Instant createdAt) {
    public record OrderLine(String sku, int quantity) {}
}
