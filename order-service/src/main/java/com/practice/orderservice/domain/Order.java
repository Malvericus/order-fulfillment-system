package com.practice.orderservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private String orderId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLineItem> items;

    protected Order() {}

    public Order(String customerId, List<OrderLineItem> items) {
        this.orderId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.status = "CREATED";
        this.createdAt = Instant.now();
        this.items = items;
        items.forEach(i -> i.setOrder(this));
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderLineItem> getItems() { return items; }
}
