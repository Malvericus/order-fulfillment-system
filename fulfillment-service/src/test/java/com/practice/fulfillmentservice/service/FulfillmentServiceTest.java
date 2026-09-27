package com.practice.fulfillmentservice.service;

import com.practice.events.InventoryReservedEvent;
import com.practice.events.OrderCreatedEvent;
import com.practice.events.ShipmentDelayedEvent;
import com.practice.events.ShipmentScheduledEvent;
import com.practice.fulfillmentservice.domain.Shipment;
import com.practice.fulfillmentservice.domain.ShipmentRepository;
import com.practice.fulfillmentservice.domain.ShipmentStatus;
import com.practice.fulfillmentservice.messaging.FulfillmentEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FulfillmentServiceTest {

    private final Map<String, Shipment> shipments = new HashMap<>();
    private ShipmentRepository shipmentRepository;
    private FulfillmentEventProducer eventProducer;
    private FulfillmentService service;

    @BeforeEach
    void setUp() {
        shipments.clear();
        shipmentRepository = mock(ShipmentRepository.class);
        eventProducer = mock(FulfillmentEventProducer.class);

        when(shipmentRepository.save(any())).thenAnswer(inv -> {
            Shipment s = inv.getArgument(0);
            shipments.put(s.getOrderId(), s);
            return s;
        });
        when(shipmentRepository.findByOrderId(anyString()))
                .thenAnswer(inv -> Optional.ofNullable(shipments.get(inv.getArgument(0, String.class))));

        service = new FulfillmentService(shipmentRepository, eventProducer);
    }

    @Test
    void onInventoryReserved_createsShipmentAndPublishesScheduled() {
        var event = new InventoryReservedEvent("order-1",
                List.of(new OrderCreatedEvent.OrderLine("sku-1", 2)), Instant.now());

        service.scheduleShipment(event);

        assertThat(shipments.get("order-1").getStatus()).isEqualTo(ShipmentStatus.SCHEDULED);
        ArgumentCaptor<ShipmentScheduledEvent> captor = ArgumentCaptor.forClass(ShipmentScheduledEvent.class);
        verify(eventProducer).publishScheduled(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo("order-1");
    }

    @Test
    void markDelayed_updatesStatusAndPublishesDelayed() {
        var event = new InventoryReservedEvent("order-2",
                List.of(new OrderCreatedEvent.OrderLine("sku-1", 1)), Instant.now());
        service.scheduleShipment(event);

        service.markDelayed("order-2", "carrier-outage");

        assertThat(shipments.get("order-2").getStatus()).isEqualTo(ShipmentStatus.DELAYED);
        ArgumentCaptor<ShipmentDelayedEvent> captor = ArgumentCaptor.forClass(ShipmentDelayedEvent.class);
        verify(eventProducer).publishDelayed(captor.capture());
        assertThat(captor.getValue().reason()).isEqualTo("carrier-outage");
    }
}
