package com.practice.events;

import java.time.Instant;
import java.util.List;

public record InventoryReservedEvent(String orderId, List<OrderCreatedEvent.OrderLine> items, Instant reservedAt) {}
