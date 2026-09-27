package com.practice.events;

import java.time.Instant;

public record OrderRejectedEvent(String orderId, String reason, Instant rejectedAt) {}
