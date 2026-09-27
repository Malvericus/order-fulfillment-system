package com.practice.orderservice.service;

import com.practice.events.OrderCreatedEvent;
import com.practice.orderservice.api.dto.CreateOrderRequest;
import com.practice.orderservice.domain.Order;
import com.practice.orderservice.domain.OrderLineItem;
import com.practice.orderservice.domain.OrderRepository;
import com.practice.orderservice.messaging.OrderEventProducer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;

    public OrderService(OrderRepository orderRepository, OrderEventProducer eventProducer) {
        this.orderRepository = orderRepository;
        this.eventProducer = eventProducer;
    }

    public Order createOrder(CreateOrderRequest request) {
        List<OrderLineItem> items = request.items().stream()
                .map(line -> new OrderLineItem(line.sku(), line.quantity()))
                .toList();
        Order saved = orderRepository.save(new Order(request.customerId(), items));

        var eventItems = saved.getItems().stream()
                .map(i -> new OrderCreatedEvent.OrderLine(i.getSku(), i.getQuantity()))
                .toList();
        eventProducer.publishOrderCreated(new OrderCreatedEvent(
                saved.getOrderId(), saved.getCustomerId(), eventItems, saved.getCreatedAt()));

        return saved;
    }
}
