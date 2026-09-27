package com.practice.fulfillmentservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    private String shipmentId;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Shipment() {}

    public Shipment(String orderId) {
        this.shipmentId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.status = ShipmentStatus.SCHEDULED;
        this.updatedAt = Instant.now();
    }

    public String getShipmentId() { return shipmentId; }
    public String getOrderId() { return orderId; }
    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; this.updatedAt = Instant.now(); }
    public Instant getUpdatedAt() { return updatedAt; }
}
