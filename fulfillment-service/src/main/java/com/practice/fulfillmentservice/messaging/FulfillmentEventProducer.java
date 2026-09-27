package com.practice.fulfillmentservice.messaging;

import com.practice.events.ShipmentDelayedEvent;
import com.practice.events.ShipmentScheduledEvent;
import com.practice.events.Topics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class FulfillmentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FulfillmentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishScheduled(ShipmentScheduledEvent event) {
        kafkaTemplate.send(Topics.SHIPMENT_SCHEDULED, event.orderId(), event);
    }

    public void publishDelayed(ShipmentDelayedEvent event) {
        kafkaTemplate.send(Topics.SHIPMENT_DELAYED, event.orderId(), event);
    }
}
