package com.practice.orderservice.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "order_line_items")
public class OrderLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    protected OrderLineItem() {}

    public OrderLineItem(String sku, int quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }

    void setOrder(Order order) { this.order = order; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
}
