package com.practice.inventoryservice.messaging;

import com.practice.events.OrderCreatedEvent;
import com.practice.events.Topics;
import com.practice.inventoryservice.service.InventoryReservationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

    private final InventoryReservationService reservationService;

    public OrderCreatedListener(InventoryReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @KafkaListener(topics = Topics.ORDER_CREATED, groupId = "inventory-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        reservationService.reserve(event);
    }
}
