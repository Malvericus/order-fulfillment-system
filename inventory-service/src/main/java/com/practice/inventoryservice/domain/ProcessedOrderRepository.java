package com.practice.inventoryservice.domain;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ProcessedOrderRepository {

    private final Map<String, Boolean> processed = new ConcurrentHashMap<>();

    public boolean hasProcessed(String orderId) {
        return processed.containsKey(orderId);
    }

    public void markProcessed(String orderId) {
        processed.put(orderId, Boolean.TRUE);
    }
}
