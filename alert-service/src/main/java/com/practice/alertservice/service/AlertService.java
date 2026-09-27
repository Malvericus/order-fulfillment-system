package com.practice.alertservice.service;

import com.practice.events.OrderRejectedEvent;
import com.practice.events.ShipmentDelayedEvent;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    private final JavaMailSender mailSender;

    public AlertService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderRejectedAlert(OrderRejectedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("ops@practice.local");
        message.setSubject("Order rejected: " + event.orderId());
        message.setText("Order " + event.orderId() + " was rejected. Reason: " + event.reason());
        mailSender.send(message);
    }

    public void sendShipmentDelayedAlert(ShipmentDelayedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("ops@practice.local");
        message.setSubject("Shipment delayed: " + event.shipmentId());
        message.setText("Shipment " + event.shipmentId() + " for order " + event.orderId()
                + " is delayed. Reason: " + event.reason());
        mailSender.send(message);
    }
}
