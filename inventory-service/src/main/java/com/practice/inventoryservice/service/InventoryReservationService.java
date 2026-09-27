package com.practice.inventoryservice.service;

import com.practice.events.InventoryReservedEvent;
import com.practice.events.OrderCreatedEvent;
import com.practice.events.OrderRejectedEvent;
import com.practice.inventoryservice.domain.InventoryRepository;
import com.practice.inventoryservice.domain.ProcessedOrderRepository;
import com.practice.inventoryservice.messaging.InventoryEventProducer;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class InventoryReservationService {

    private final InventoryRepository inventoryRepository;
    private final ProcessedOrderRepository processedOrderRepository;
    private final InventoryEventProducer eventProducer;

    public InventoryReservationService(InventoryRepository inventoryRepository,
                                        ProcessedOrderRepository processedOrderRepository,
                                        InventoryEventProducer eventProducer) {
        this.inventoryRepository = inventoryRepository;
        this.processedOrderRepository = processedOrderRepository;
        this.eventProducer = eventProducer;
    }

    public void reserve(OrderCreatedEvent event) {
        if (processedOrderRepository.hasProcessed(event.orderId())) {
            return;
        }

        boolean sufficientStock = event.items().stream().allMatch(line ->
                inventoryRepository.findBySku(line.sku())
                        .map(item -> item.getQuantityAvailable() >= line.quantity())
                        .orElse(false));

        if (!sufficientStock) {
            eventProducer.publishRejected(new OrderRejectedEvent(event.orderId(), "OUT_OF_STOCK", Instant.now()));
            processedOrderRepository.markProcessed(event.orderId());
            return;
        }

        event.items().forEach(line -> {
            var item = inventoryRepository.findBySku(line.sku()).orElseThrow();
            item.setQuantityAvailable(item.getQuantityAvailable() - line.quantity());
            inventoryRepository.save(item);
        });

        processedOrderRepository.markProcessed(event.orderId());
        eventProducer.publishReserved(new InventoryReservedEvent(event.orderId(), event.items(), Instant.now()));
    }
}
