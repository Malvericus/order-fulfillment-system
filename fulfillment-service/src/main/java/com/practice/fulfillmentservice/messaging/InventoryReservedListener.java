package com.practice.fulfillmentservice.messaging;

import com.practice.events.InventoryReservedEvent;
import com.practice.events.Topics;
import com.practice.fulfillmentservice.service.FulfillmentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservedListener {

    private final FulfillmentService fulfillmentService;

    public InventoryReservedListener(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @KafkaListener(topics = Topics.INVENTORY_RESERVED, groupId = "fulfillment-service")
    public void onInventoryReserved(InventoryReservedEvent event) {
        fulfillmentService.scheduleShipment(event);
    }
}
