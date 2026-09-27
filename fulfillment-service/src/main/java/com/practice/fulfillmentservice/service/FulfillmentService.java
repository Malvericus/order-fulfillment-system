package com.practice.fulfillmentservice.service;

import com.practice.events.InventoryReservedEvent;
import com.practice.events.ShipmentDelayedEvent;
import com.practice.events.ShipmentScheduledEvent;
import com.practice.fulfillmentservice.domain.Shipment;
import com.practice.fulfillmentservice.domain.ShipmentRepository;
import com.practice.fulfillmentservice.domain.ShipmentStatus;
import com.practice.fulfillmentservice.messaging.FulfillmentEventProducer;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class FulfillmentService {

    private final ShipmentRepository shipmentRepository;
    private final FulfillmentEventProducer eventProducer;

    public FulfillmentService(ShipmentRepository shipmentRepository, FulfillmentEventProducer eventProducer) {
        this.shipmentRepository = shipmentRepository;
        this.eventProducer = eventProducer;
    }

    public void scheduleShipment(InventoryReservedEvent event) {
        Shipment shipment = shipmentRepository.save(new Shipment(event.orderId()));
        eventProducer.publishScheduled(new ShipmentScheduledEvent(event.orderId(), shipment.getShipmentId(), Instant.now()));
    }

    public void markDelayed(String orderId, String reason) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId).orElseThrow();
        shipment.setStatus(ShipmentStatus.DELAYED);
        shipmentRepository.save(shipment);
        eventProducer.publishDelayed(new ShipmentDelayedEvent(orderId, shipment.getShipmentId(), reason, Instant.now()));
    }
}
