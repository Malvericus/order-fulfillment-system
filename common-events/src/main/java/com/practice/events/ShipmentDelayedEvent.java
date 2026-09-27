package com.practice.events;

import java.time.Instant;

public record ShipmentDelayedEvent(String orderId, String shipmentId, String reason, Instant delayedAt) {}
