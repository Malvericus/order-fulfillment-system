package com.practice.inventoryservice.messaging;

import com.practice.events.InventoryReservedEvent;
import com.practice.events.OrderRejectedEvent;
import com.practice.events.Topics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishReserved(InventoryReservedEvent event) {
        kafkaTemplate.send(Topics.INVENTORY_RESERVED, event.orderId(), event);
    }

    public void publishRejected(OrderRejectedEvent event) {
        kafkaTemplate.send(Topics.ORDER_REJECTED, event.orderId(), event);
    }
}
