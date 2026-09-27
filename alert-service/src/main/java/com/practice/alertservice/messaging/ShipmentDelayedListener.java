package com.practice.alertservice.messaging;

import com.practice.events.ShipmentDelayedEvent;
import com.practice.events.Topics;
import com.practice.alertservice.service.AlertService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ShipmentDelayedListener {

    private final AlertService alertService;

    public ShipmentDelayedListener(AlertService alertService) {
        this.alertService = alertService;
    }

    @KafkaListener(topics = Topics.SHIPMENT_DELAYED, groupId = "alert-service")
    public void onShipmentDelayed(ShipmentDelayedEvent event) {
        alertService.sendShipmentDelayedAlert(event);
    }
}
