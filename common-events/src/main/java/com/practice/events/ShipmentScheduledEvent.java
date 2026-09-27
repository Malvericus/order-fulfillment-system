package com.practice.events;

import java.time.Instant;

public record ShipmentScheduledEvent(String orderId, String shipmentId, Instant scheduledAt) {}
