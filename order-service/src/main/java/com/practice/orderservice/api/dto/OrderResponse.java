package com.practice.orderservice.api.dto;

import com.practice.orderservice.domain.Order;

import java.time.Instant;

public record OrderResponse(String orderId, String customerId, String status, Instant createdAt) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getOrderId(), order.getCustomerId(), order.getStatus(), order.getCreatedAt());
    }
}
