package com.practice.inventoryservice.service;

import com.practice.events.InventoryReservedEvent;
import com.practice.events.OrderCreatedEvent;
import com.practice.events.OrderRejectedEvent;
import com.practice.inventoryservice.domain.InventoryItem;
import com.practice.inventoryservice.domain.InventoryRepository;
import com.practice.inventoryservice.domain.ProcessedOrderRepository;
import com.practice.inventoryservice.messaging.InventoryEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InventoryReservationServiceTest {

    private final Map<String, InventoryItem> stock = new HashMap<>();
    private final Set<String> processedOrderIds = new HashSet<>();
    private InventoryRepository inventoryRepository;
    private ProcessedOrderRepository processedOrderRepository;
    private InventoryEventProducer eventProducer;
    private InventoryReservationService service;

    @BeforeEach
    void setUp() {
        stock.clear();
        processedOrderIds.clear();
        inventoryRepository = mock(InventoryRepository.class);
        processedOrderRepository = mock(ProcessedOrderRepository.class);
        eventProducer = mock(InventoryEventProducer.class);

        when(inventoryRepository.findBySku(anyString()))
                .thenAnswer(inv -> Optional.ofNullable(stock.get(inv.getArgument(0, String.class))));
        doAnswer(inv -> { InventoryItem item = inv.getArgument(0); stock.put(item.getSku(), item); return null; })
                .when(inventoryRepository).save(any());
        when(processedOrderRepository.hasProcessed(anyString()))
                .thenAnswer(inv -> processedOrderIds.contains(inv.getArgument(0, String.class)));
        doAnswer(inv -> { processedOrderIds.add(inv.getArgument(0, String.class)); return null; })
                .when(processedOrderRepository).markProcessed(anyString());

        service = new InventoryReservationService(inventoryRepository, processedOrderRepository, eventProducer);
    }

    @Test
    void reserve_withSufficientStock_decrementsStockAndPublishesReserved() {
        stock.put("sku-1", new InventoryItem("sku-1", 10));
        var event = new OrderCreatedEvent("order-1", "cust-1",
                List.of(new OrderCreatedEvent.OrderLine("sku-1", 3)), Instant.now());

        service.reserve(event);

        assertThat(stock.get("sku-1").getQuantityAvailable()).isEqualTo(7);
        ArgumentCaptor<InventoryReservedEvent> captor = ArgumentCaptor.forClass(InventoryReservedEvent.class);
        verify(eventProducer).publishReserved(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo("order-1");
        verify(eventProducer, never()).publishRejected(any());
    }

    @Test
    void reserve_withInsufficientStock_publishesRejectedAndDoesNotThrow() {
        stock.put("sku-1", new InventoryItem("sku-1", 1));
        var event = new OrderCreatedEvent("order-2", "cust-1",
                List.of(new OrderCreatedEvent.OrderLine("sku-1", 5)), Instant.now());

        service.reserve(event);

        ArgumentCaptor<OrderRejectedEvent> captor = ArgumentCaptor.forClass(OrderRejectedEvent.class);
        verify(eventProducer).publishRejected(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo("order-2");
        assertThat(captor.getValue().reason()).isEqualTo("OUT_OF_STOCK");
        verify(eventProducer, never()).publishReserved(any());
        assertThat(stock.get("sku-1").getQuantityAvailable()).isEqualTo(1);
    }

    @Test
    void reserve_calledTwiceWithSameOrderId_onlyDecrementsStockOnce() {
        stock.put("sku-1", new InventoryItem("sku-1", 10));
        var event = new OrderCreatedEvent("order-3", "cust-1",
                List.of(new OrderCreatedEvent.OrderLine("sku-1", 3)), Instant.now());

        service.reserve(event);
        service.reserve(event);

        assertThat(stock.get("sku-1").getQuantityAvailable()).isEqualTo(7);
        verify(eventProducer, times(1)).publishReserved(any());
    }
}
